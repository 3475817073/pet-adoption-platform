package com.petplatform.petadoption.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置类
 * 针对前后端分离架构进行简化配置，开放所有接口权限并禁用默认认证机制
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     * @param http HttpSecurity 对象，用于配置 HTTP 安全策略
     * @return SecurityFilterChain 构建完成的安全过滤链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /*
         * 关闭 CSRF 防护、允许所有请求访问、并禁用默认的表单登录与 Basic Auth
         * 适用于前后端分离且由应用层自行处理身份校验的场景
         */
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
