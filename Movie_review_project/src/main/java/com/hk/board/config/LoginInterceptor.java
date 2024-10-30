package com.hk.board.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        if (session.getAttribute("mdto") == null) { // 로그인하지 않은 경우
            System.out.println("로그인이 필요합니다.");
            response.sendRedirect("/"); // 메인 화면으로 이동
            return false;
        }
        return true; // 로그인된 경우 요청 허용
    }
}
