package dev.yukitrail.api.auth.model;

import java.time.LocalDateTime;

<<<<<<< HEAD
/** 与 users 表对应的内部账户模型。 */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
public class UserAccount {

    private Long id;
    private String email;
<<<<<<< HEAD
    /** DelegatingPasswordEncoder 生成的密码哈希，禁止保存或输出明文密码。 */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
    private String passwordHash;
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
