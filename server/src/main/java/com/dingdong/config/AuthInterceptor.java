package com.dingdong.config;

import com.dingdong.common.context.SystemContextHolder;
import com.dingdong.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

/**
 * JWT Token 认证拦截器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    /**
     * 请求头中的 Token 字段名
     */
    private static final String HEADER_TOKEN = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供认证令牌");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌无效或已过期");
            return false;
        }

        // 获取用户ID并存入上下文
        Long userId = jwtUtil.getUserIdFromToken(token);
        SystemContextHolder.setUserId(userId);

        String username = jwtUtil.getNicknameFromToken(token);
        SystemContextHolder.setUsername(username);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        // 请求结束后清除上下文，避免内存泄漏
        SystemContextHolder.clear();
    }

    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_TOKEN);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        // 兼容直接传 token（不带 Bearer 前缀）
        return bearerToken;
    }

    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> result = Map.of(
                "code", status,
                "msg", message,
                "data", (Object) null);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
