package com.wedit.weditapp.global.auth.login.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (exception.getMessage().contains("authorization_request_not_found")) {
            log.warn("OAuth2 로그인 요청이 세션에서 만료되었습니다. (무시 가능)");
            response.sendRedirect("/login"); // 로그인 페이지로 리다이렉트
            return;
        }

        log.error("소셜 로그인 실패! 에러 메시지: {}", exception.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"OAuth2 login failed\", \"message\": \"" + exception.getMessage() + "\"}");
    }
}
