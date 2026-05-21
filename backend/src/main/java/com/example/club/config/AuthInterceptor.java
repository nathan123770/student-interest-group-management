package com.example.club.config;

import com.example.club.exception.BusinessException;
import com.example.club.utils.AuthContext;
import com.example.club.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtUtils jwtUtils;

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("GET".equalsIgnoreCase(request.getMethod()) && "/api/categories".equals(request.getRequestURI())) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }
        Claims claims;
        try {
            claims = jwtUtils.parse(header.substring(7));
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }
        AuthContext.set(Long.valueOf(claims.getSubject()), (List<String>) claims.get("roles"));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
