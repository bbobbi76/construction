package com.domain.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 웹 브라우저가 /uploads/** (예: /uploads/photo.jpg)로 요청할 때,
     * 서버의 'uploads/' (file:uploads/) 폴더에서 파일을 찾아 제공하도록 설정합니다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/"); // (file: <- 실제 파일 시스템 경로)
    }
}