package dev.yukitrail.api.auth.dto;

<<<<<<< HEAD
/** 认证成功后返回给前端的访问令牌和当前用户信息。 */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
}
