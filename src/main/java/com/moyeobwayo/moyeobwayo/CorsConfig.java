package com.moyeobwayo.moyeobwayo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000", "https://www.moyeobwayo.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 허용할 메소드
                .allowedHeaders("*") // 허용할 헤더
                .allowCredentials(true); // 쿠키를 포함한 요청을 허용할지 여부
    }
}
