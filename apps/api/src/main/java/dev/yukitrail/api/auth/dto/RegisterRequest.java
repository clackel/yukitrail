package dev.yukitrail.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

<<<<<<< HEAD
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
=======
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
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
    public RegisterRequest {
        email = email == null ? null : email.strip();
        nickname = nickname == null ? null : nickname.strip();
    }
}
