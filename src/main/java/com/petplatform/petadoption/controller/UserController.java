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
            result.put("createTime", user.getCreateTime());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
