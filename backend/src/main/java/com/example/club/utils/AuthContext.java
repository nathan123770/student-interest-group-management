package com.example.club.utils;

import com.example.club.exception.BusinessException;

import java.util.List;

public class AuthContext {
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> ROLES = new ThreadLocal<>();

    public static void set(Long userId, List<String> roles) {
        USER_ID.set(userId);
        ROLES.set(roles);
    }

    public static Long userId() {
        Long userId = USER_ID.get();
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        return userId;
    }

    public static List<String> roles() {
        return ROLES.get() == null ? List.of() : ROLES.get();
    }

    public static boolean hasRole(String role) {
        return roles().contains(role);
    }

    public static void requireAny(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return;
            }
        }
        throw new BusinessException(403, "无权访问该功能");
    }

    public static void clear() {
        USER_ID.remove();
        ROLES.remove();
    }
}
