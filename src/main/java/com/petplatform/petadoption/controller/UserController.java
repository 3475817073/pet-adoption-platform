package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.entity.Role;
import com.petplatform.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.petplatform.petadoption.entity.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 注册接口
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest().body("用户名已存在！");
            }

            if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                String phoneRegex = "^1[3-9]\\d{9}$";
                if (!user.getPhone().matches(phoneRegex)) {
                    return ResponseEntity.badRequest().body("请输入正确的手机号");
                }
            }

            user.setRole(Role.USER);
            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            userService.save(user);
            return ResponseEntity.ok("注册成功");

        } catch (Exception e) {
            // 打印详细错误
            e.printStackTrace();
            return ResponseEntity.status(500).body("注册系统异常：" + e.getMessage());
        }
    }

    /**
     * 登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String inputPassword = credentials.get("password");

            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(401).body("用户不存在");
            }

            // 使用 BCrypt 进行统一比对
            if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
                return ResponseEntity.status(401).body("密码错误");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("role", user.getRole());
            result.put("avatar", user.getAvatar());
            result.put("bio", user.getBio());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("登录系统异常：" + e.getMessage());
        }
    }

    /**
     * 根据用户名获取用户基本信息（用于个人主页）
     */
    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", user.getId());
            result.put("username", user.getUsername());
            result.put("realName", user.getRealName());
            result.put("role", user.getRole());
            result.put("avatar", user.getAvatar());
            result.put("bio", user.getBio());
            result.put("createTime", user.getCreateTime());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 分页获取用户列表（仅管理员可用）
     */
    @GetMapping("/list")
    public ResponseEntity<?> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        try {
            org.springframework.data.domain.Pageable pageable =
                    org.springframework.data.domain.PageRequest.of(page, size);
            org.springframework.data.domain.Page<User> users;
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userService.searchUsers(keyword.trim(), pageable);
            } else {
                users = userService.getUsers(pageable);
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 删除用户（仅管理员可用）
     */
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId, @RequestParam String adminUsername) {
        try {
            // 验证管理员权限
            User admin = userService.findByUsername(adminUsername);
            if (admin == null || admin.getRole() != com.petplatform.petadoption.entity.Role.ADMIN) {
                return ResponseEntity.status(403).body("无权限执行此操作");
            }

            // 不能删除自己
            if (admin.getId().equals(userId)) {
                return ResponseEntity.badRequest().body("不能删除自己的账号");
            }

            // 检查用户是否存在
            User targetUser = userService.findById(userId);
            if (targetUser == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            // 执行删除（级联删除所有相关数据）
            userService.deleteUser(userId);
            return ResponseEntity.ok("用户删除成功");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        User user = userService.findByUsername(username);
        if (user == null) return ResponseEntity.badRequest().body("用户不存在");

        if (request.containsKey("avatar") && request.get("avatar") != null && !request.get("avatar").isEmpty()) {
            user.setAvatar(request.get("avatar"));
        }
        if (request.containsKey("bio") && request.get("bio") != null && !request.get("bio").isEmpty()) {
            user.setBio(request.get("bio"));
        }

        if (request.containsKey("newPassword")) {
            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");
            if (oldPassword != null && !passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ResponseEntity.status(401).body("原密码错误");
            }
            if (newPassword.length() < 6) return ResponseEntity.badRequest().body("密码至少6位");
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        userService.save(user);
        return ResponseEntity.ok(user);
    }



}
