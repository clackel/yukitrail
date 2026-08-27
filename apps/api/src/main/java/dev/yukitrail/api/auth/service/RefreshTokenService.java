package dev.yukitrail.api.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

/** 生成高熵刷新令牌，并计算可安全持久化的 SHA-256 摘要。 */
@Service
public class RefreshTokenService {

    private final SecureRandom secureRandom = new SecureRandom();

    /** 生成 32 字节安全随机数，并编码为无填充的 URL-safe Base64。 */
    public String generate() {
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    /** 计算刷新令牌摘要，数据库不会保存原始令牌。 */
    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 Java 运行环境不支持 SHA-256 算法", exception);
        }
    }
}
