package com.petplatform.petadoption.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 用于配置静态资源映射，实现上传文件的本地访问
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 从配置文件中读取的本地上传文件根路径 */
    @Value("${spring.upload.path}")
    private String uploadPath;

    /**
     * 添加静态资源处理器，将 URL 请求映射到本地文件系统
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /*
         * 将所有以 /uploads/ 开头的请求映射到本地文件系统的 uploadPath 目录下
         * 使得前端可以通过 HTTP 请求直接访问已上传的图片或文件
         */
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
