package dev.yukitrail.api.auth.dto;

import dev.yukitrail.api.auth.model.UserAccount;

/** 对外公开的用户信息，不包含密码哈希等敏感字段。 */
public record UserResponse(
        long id,
        String email,
        String nickname
) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
