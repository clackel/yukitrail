package dev.yukitrail.api.auth.dto;

/** 认证成功后返回给前端的访问令牌和当前用户信息。 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
