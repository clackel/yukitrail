package dev.yukitrail.api.auth;

import dev.yukitrail.api.auth.config.AuthProperties;
import dev.yukitrail.api.auth.dto.AuthResponse;
import dev.yukitrail.api.auth.dto.LoginRequest;
import dev.yukitrail.api.auth.dto.RegisterRequest;
import dev.yukitrail.api.auth.dto.UserResponse;
import dev.yukitrail.api.auth.error.InvalidRefreshSessionException;
import dev.yukitrail.api.auth.service.AuthService;
import dev.yukitrail.api.auth.service.AuthService.IssuedAuthSession;
import dev.yukitrail.api.common.api.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账户认证接口。
 *
 * <p>访问令牌通过响应体返回，刷新令牌只通过 HttpOnly Cookie 传递，避免前端脚本读取。</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthProperties properties;

    public AuthController(AuthService authService, AuthProperties properties) {
        this.authService = authService;
        this.properties = properties;
    }

    /** 注册账户并立即创建登录会话。 */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        IssuedAuthSession session = authService.register(body);
        writeRefreshCookie(response, session.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(session.response(), request));
    }

    /** 校验邮箱和密码，成功后签发访问令牌和刷新 Cookie。 */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        IssuedAuthSession session = authService.login(body);
        writeRefreshCookie(response, session.refreshToken());
        return ApiResponse.ok(session.response(), request);
    }

    /** 轮换刷新会话；旧令牌从本次请求成功后立即失效。 */
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            IssuedAuthSession session = authService.refresh(findRefreshToken(request));
            writeRefreshCookie(response, session.refreshToken());
            return ApiResponse.ok(session.response(), request);
        } catch (InvalidRefreshSessionException exception) {
            // 无效 Cookie 也要主动清除，避免浏览器在后续请求中反复携带。
            clearRefreshCookie(response);
            throw exception;
        }
    }

    /** 幂等退出：无 Cookie 或会话已撤销时也返回成功。 */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(findRefreshToken(request));
        clearRefreshCookie(response);
        return ApiResponse.ok(null, request);
    }

    /** 根据 Spring Security 已验证的 JWT subject 查询当前用户。 */
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(Authentication authentication, HttpServletRequest request) {
        return ApiResponse.ok(authService.currentUser(authentication.getName()), request);
    }

    private void writeRefreshCookie(HttpServletResponse response, String refreshToken) {
        // Cookie 只允许认证接口携带，减少刷新令牌暴露给其他请求的范围。
        ResponseCookie cookie = ResponseCookie.from(properties.getCookieName(), refreshToken)
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .sameSite("Lax")
                .path(properties.getCookiePath())
                .maxAge(properties.getRefreshTokenTtl())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(properties.getCookieName(), "")
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .sameSite("Lax")
                .path(properties.getCookiePath())
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String findRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (properties.getCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
