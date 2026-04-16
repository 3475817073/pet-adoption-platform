package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户业务逻辑层
 * 负责处理用户注册、登录校验及信息查询等核心业务
 */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    /**
     * 根据用户名查找用户信息
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 检查用户名是否已被注册
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * 保存或更新用户信息（用于注册或资料修改）
     */
    public User save(User user) {
        return userRepository.save(user);
    }
}
