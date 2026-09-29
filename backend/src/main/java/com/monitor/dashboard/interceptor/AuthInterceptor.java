package com.monitor.dashboard.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.dashboard.common.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 环境开关式接口鉴权（默认关闭，保持与原行为兼容）。
 * 设置 REQUIRE_AUTH=true 后，非 GET 的 /api/** 必须携带 Bearer Token 才能访问；
 * 只读 GET 与 /api/actuator/**（已在 WebMvcConfig 排除）始终放行，以保证健康检查可达。
 * 注意：本服务当前未签发 JWT，开启后仅做「是否携带 Bearer」的准入校验；
 * 若后续接入统一鉴权，请在此补充 JWT 验签逻辑（可参考 competition / activity 项目的 JwtInterceptor）。
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Value("${REQUIRE_AUTH:false}")
    private boolean requireAuth;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!requireAuth) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 只读 GET 允许匿名访问（看板数据只读场景）
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        boolean hasBearer = authHeader != null && authHeader.startsWith("Bearer ")
                && authHeader.trim().length() > "Bearer ".length();
        if (!hasBearer) {
            log.warn("鉴权拒绝（缺失 Bearer）: {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(R.fail(401, "未登录或token缺失")));
            return false;
        }
        return true;
    }
}
