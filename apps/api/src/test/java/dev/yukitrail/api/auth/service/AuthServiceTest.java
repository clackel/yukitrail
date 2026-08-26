package dev.yukitrail.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import dev.yukitrail.api.auth.config.AuthProperties;
import dev.yukitrail.api.auth.dto.RegisterRequest;
import dev.yukitrail.api.auth.mapper.AuthSessionMapper;
import dev.yukitrail.api.auth.mapper.UserMapper;
import dev.yukitrail.api.auth.model.AuthSession;
import dev.yukitrail.api.auth.model.UserAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthSessionMapper authSessionMapper;

    @Mock
    private JwtTokenService jwtTokenService;

    private PasswordEncoder passwordEncoder;
    private RefreshTokenService refreshTokenService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        refreshTokenService = new RefreshTokenService();
        AuthProperties properties = new AuthProperties();
        Clock clock = Clock.fixed(Instant.parse("2026-08-26T12:00:00Z"), ZoneOffset.UTC);
        authService = new AuthService(
                userMapper,
                authSessionMapper,
                passwordEncoder,
                jwtTokenService,
                refreshTokenService,
                properties,
                clock
        );
    }

    @Test
    void registrationNormalizesIdentityAndStoresOnlyProtectedSecrets() {
        when(userMapper.findByEmail("traveler@example.com")).thenReturn(null);
        doAnswer(invocation -> {
            UserAccount user = invocation.getArgument(0);
            user.setId(7L);
            return 1;
        }).when(userMapper).insert(any(UserAccount.class));
        when(jwtTokenService.issue(7L)).thenReturn(new JwtTokenService.AccessToken("access-token", 900));

        AuthService.IssuedAuthSession result = authService.register(new RegisterRequest(
                " Traveler@Example.com ",
                "correct-horse",
                "  雪路  "
        ));

        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userMapper).insert(userCaptor.capture());
        UserAccount savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("traveler@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("雪路");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("correct-horse");
        assertThat(passwordEncoder.matches("correct-horse", savedUser.getPasswordHash())).isTrue();

        ArgumentCaptor<AuthSession> sessionCaptor = ArgumentCaptor.forClass(AuthSession.class);
        verify(authSessionMapper).insert(sessionCaptor.capture());
        AuthSession savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getRefreshTokenHash())
                .isEqualTo(refreshTokenService.hash(result.refreshToken()))
                .doesNotContain(result.refreshToken());
        assertThat(result.response().accessToken()).isEqualTo("access-token");
        assertThat(result.response().user().id()).isEqualTo(7L);
    }
}
