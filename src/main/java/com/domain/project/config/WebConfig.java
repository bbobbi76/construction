package com.domain.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * [웹 MVC 설정]
 * 정적 리소스(이미지, 첨부파일 등)에 대한 경로 매핑을 담당하는 설정 클래스입니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * [리소스 핸들러 추가]
     * 사용자가 업로드한 파일이 저장된 서버의 물리적 폴더를 웹 URL 경로와 연결합니다.
     * * - 동작 방식: 웹 브라우저가 "/uploads/**" 패턴으로 요청하면,
     * 서버의 "uploads/" 폴더에서 해당 파일을 찾아 반환합니다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")      // 1. 웹에서 접근할 URL 패턴 (예: /uploads/abc.jpg)
                .addResourceLocations("file:uploads/"); // 2. 실제 파일이 위치한 서버 경로 (file: 접두어는 로컬 파일 시스템을 의미)
    }
}