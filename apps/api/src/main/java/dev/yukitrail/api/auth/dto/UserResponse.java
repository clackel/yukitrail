package dev.yukitrail.api.auth.dto;

import dev.yukitrail.api.auth.model.UserAccount;

public record UserResponse(
        long id,
        String email,
        String nickname
) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}
