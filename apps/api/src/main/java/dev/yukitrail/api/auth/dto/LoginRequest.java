package dev.yukitrail.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 登录请求及其服务端输入约束。 */
public record LoginRequest(
        @NotBlank(message = "请输入邮箱")
        @Email(message = "请输入有效的邮箱地址")
        @Size(max = 191, message = "邮箱长度不能超过 191 个字符")
        String email,

        @NotBlank(message = "请输入密码")
        @Size(min = 8, max = 72, message = "密码长度必须为 8–72 个字符")
        String password
) {
    /** 在进入业务层之前先清理邮箱首尾空格。 */
    public LoginRequest {
        email = email == null ? null : email.strip();
    }
}
