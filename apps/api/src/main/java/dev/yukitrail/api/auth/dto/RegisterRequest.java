package dev.yukitrail.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 注册请求及其服务端输入约束。 */
public record RegisterRequest(
        @NotBlank(message = "请输入邮箱")
        @Email(message = "请输入有效的邮箱地址")
        @Size(max = 191, message = "邮箱长度不能超过 191 个字符")
        String email,

        @NotBlank(message = "请输入密码")
        @Size(min = 8, max = 72, message = "密码长度必须为 8–72 个字符")
        String password,

        @NotBlank(message = "请输入昵称")
        @Size(min = 2, max = 40, message = "昵称长度必须为 2–40 个字符")
        String nickname
) {
    /** 昵称和邮箱在校验、持久化前统一移除首尾空格。 */
    public RegisterRequest {
        email = email == null ? null : email.strip();
        nickname = nickname == null ? null : nickname.strip();
    }
}
