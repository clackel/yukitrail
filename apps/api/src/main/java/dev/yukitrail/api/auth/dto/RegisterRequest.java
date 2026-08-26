package dev.yukitrail.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 191, message = "Email must not exceed 191 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        @NotBlank(message = "Nickname is required")
        @Size(min = 2, max = 40, message = "Nickname must be between 2 and 40 characters")
        String nickname
) {
    public RegisterRequest {
        email = email == null ? null : email.strip();
        nickname = nickname == null ? null : nickname.strip();
    }
}
