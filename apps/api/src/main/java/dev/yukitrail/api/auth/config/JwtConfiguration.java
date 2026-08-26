package dev.yukitrail.api.auth.config;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.util.Base64;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * JWT、RSA 密钥和密码编码器配置。
 *
 * <p>本地未提供密钥时生成临时密钥；部署环境可通过开关强制要求固定密钥。</p>
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class JwtConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JwtConfiguration.class);

    /** 读取部署密钥，或在本地开发模式下生成进程级临时密钥。 */
    @Bean
    RsaKeyPair rsaKeyPair(AuthProperties properties) {
        boolean hasPrivateKey = hasText(properties.getPrivateKey());
        boolean hasPublicKey = hasText(properties.getPublicKey());

        if (hasPrivateKey != hasPublicKey) {
            throw new IllegalStateException("JWT_PRIVATE_KEY 与 JWT_PUBLIC_KEY 必须同时配置");
        }

        if (hasPrivateKey) {
            return decodeKeyPair(properties.getPrivateKey(), properties.getPublicKey());
        }

        if (properties.isKeysRequired()) {
            throw new IllegalStateException("当前环境要求固定 JWT 密钥，但尚未完成配置");
        }

        log.warn("未配置 JWT 密钥，正在为本地开发生成内存临时 RSA 密钥；应用重启后旧访问令牌将失效");
        return generateKeyPair();
    }

    @Bean
    JwtEncoder jwtEncoder(RsaKeyPair keyPair) {
        RSAKey rsaKey = new RSAKey.Builder(keyPair.publicKey())
                .privateKey(keyPair.privateKey())
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<SecurityContext>(new JWKSet(rsaKey)));
    }

    /** 只接受 RS256，并同时校验标准时间字段和配置的签发者。 */
    @Bean
    JwtDecoder jwtDecoder(RsaKeyPair keyPair, AuthProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(keyPair.publicKey())
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getIssuer()));
        return decoder;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    private static RsaKeyPair decodeKeyPair(String privateKey, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKey decodedPrivateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(decodeBase64(privateKey))
            );
            RSAPublicKey decodedPublicKey = (RSAPublicKey) keyFactory.generatePublic(
                    new X509EncodedKeySpec(decodeBase64(publicKey))
            );
            return new RsaKeyPair(decodedPublicKey, decodedPrivateKey);
        } catch (Exception exception) {
            throw new IllegalStateException("配置的 JWT 密钥不是有效的 RSA 密钥", exception);
        }
    }

    private static RsaKeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            return new RsaKeyPair(
                    (RSAPublicKey) keyPair.getPublic(),
                    (RSAPrivateKey) keyPair.getPrivate()
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 Java 运行环境不支持 RSA 算法", exception);
        }
    }

    private static byte[] decodeBase64(String value) {
        return Base64.getDecoder().decode(value.replaceAll("\\s", ""));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record RsaKeyPair(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
    }
}
