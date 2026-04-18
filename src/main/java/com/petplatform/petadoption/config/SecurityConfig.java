package com.petplatform.petadoption.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 关闭 CSRF 防御（前后端分离项目通常需要关闭，否则会报 403）
                .csrf(csrf -> csrf.disable())

                // 2. 开启并配置 CORS（解决你看到的 'Access-Control-Allow-Origin' 报错）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. 暂时放开所有接口的访问权限（允许登录/注册接口被访问）
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
                );

        return http.build();
    }

    // 定义跨域配置源
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有来源（开发阶段方便，生产环境建议指定具体域名）
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        // 允许所有请求方法（GET, POST, PUT, DELETE 等）
        configuration.setAllowedMethods(Arrays.asList("*"));
        // 允许所有请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允许携带 Cookie/Token 信息
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
