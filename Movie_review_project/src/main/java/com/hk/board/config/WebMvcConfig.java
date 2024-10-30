package com.hk.board.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .order(1) // 우선순위 설정
                .addPathPatterns("/board/**") // 게시판 경로에만 적용
                .excludePathPatterns("/user/login", "/user/addUser", "/css/**", "/js/**", "/"); // 예외 경로 설정
    }
}
