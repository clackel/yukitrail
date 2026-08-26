package dev.yukitrail.api.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import dev.yukitrail.api.auth.config.JwtConfiguration.RsaKeyPair;
import dev.yukitrail.api.auth.service.JwtTokenService;
import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

class JwtConfigurationTest {

    @Test
    void issuesAndValidatesAnRs256AccessToken() {
        AuthProperties properties = new AuthProperties();
        properties.setIssuer("https://api.yukitrail.test");
        properties.setAccessTokenTtl(Duration.ofMinutes(15));

        JwtConfiguration configuration = new JwtConfiguration();
        RsaKeyPair keyPair = configuration.rsaKeyPair(properties);
        JwtEncoder encoder = configuration.jwtEncoder(keyPair);
        JwtDecoder decoder = configuration.jwtDecoder(keyPair, properties);
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        JwtTokenService service = new JwtTokenService(
                encoder,
                properties,
                Clock.fixed(now, ZoneOffset.UTC)
        );

        JwtTokenService.AccessToken token = service.issue(42L);
        Jwt decoded = decoder.decode(token.value());

        assertThat(token.expiresIn()).isEqualTo(900);
        assertThat(decoded.getSubject()).isEqualTo("42");
        assertThat(decoded.getIssuer().toString()).isEqualTo("https://api.yukitrail.test");
        assertThat(decoded.getIssuedAt()).isEqualTo(now);
        assertThat(decoded.getExpiresAt()).isEqualTo(now.plusSeconds(900));
        assertThat(decoded.getId()).isNotBlank();
    }

    @Test
    void requiresConfiguredKeysWhenTheDeploymentFlagIsEnabled() {
        AuthProperties properties = new AuthProperties();
        properties.setKeysRequired(true);

        assertThatThrownBy(() -> new JwtConfiguration().rsaKeyPair(properties))
                .isInstanceOf(IllegalStateException.class)
<<<<<<< HEAD
                .hasMessageContaining("要求固定 JWT 密钥");
=======
                .hasMessageContaining("required");
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
    }

    @Test
    void rejectsAnExpiredAccessToken() {
        AuthProperties properties = new AuthProperties();
        properties.setIssuer("https://api.yukitrail.test");
        properties.setAccessTokenTtl(Duration.ofMinutes(15));

        JwtConfiguration configuration = new JwtConfiguration();
        RsaKeyPair keyPair = configuration.rsaKeyPair(properties);
        JwtEncoder encoder = configuration.jwtEncoder(keyPair);
        JwtDecoder decoder = configuration.jwtDecoder(keyPair, properties);
        JwtTokenService service = new JwtTokenService(
                encoder,
                properties,
                Clock.fixed(Instant.now().minus(Duration.ofHours(1)), ZoneOffset.UTC)
        );

        JwtTokenService.AccessToken token = service.issue(42L);

        assertThatThrownBy(() -> decoder.decode(token.value()))
                .isInstanceOf(JwtValidationException.class);
    }
}
