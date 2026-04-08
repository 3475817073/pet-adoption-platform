package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.entity.Role;
import com.petplatform.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")   // 允许前端跨域访问
public class UserController {

    private final UserService userService;

    // 注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("用户名已存在！");
        }
        // 简单设置默认角色（前端传 role）
        if (user.getRole() == null) {
            user.setRole(Role.ADOPTER);  // 默认领养者
        }
        User saved = userService.save(user);
        return ResponseEntity.ok("注册成功！你的ID是：" + saved.getId());
    }

    // 登录
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginUser) {
        User user = userService.findByUsername(loginUser.getUsername());
        if (user == null || !user.getPassword().equals(loginUser.getPassword())) {
            return ResponseEntity.badRequest().body("用户名或密码错误！");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", "登录成功！");
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        result.put("realName", user.getRealName());
        return ResponseEntity.ok(result);
    }
}