package com.petplatform.petadoption.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())                    // 关闭CSRF（前后端分离常用）
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()                    // 允许所有接口访问（毕业设计够用）
                )
                .formLogin(form -> form.disable())               // 关闭默认登录页面
                .httpBasic(basic -> basic.disable());            // 关闭Basic Auth

        return http.build();
    }
}