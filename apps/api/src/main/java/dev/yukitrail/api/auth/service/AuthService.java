package dev.yukitrail.api.auth.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.UUID;

import dev.yukitrail.api.auth.config.AuthProperties;
import dev.yukitrail.api.auth.dto.AuthResponse;
import dev.yukitrail.api.auth.dto.LoginRequest;
import dev.yukitrail.api.auth.dto.RegisterRequest;
import dev.yukitrail.api.auth.dto.UserResponse;
import dev.yukitrail.api.auth.error.InvalidRefreshSessionException;
import dev.yukitrail.api.auth.mapper.AuthSessionMapper;
import dev.yukitrail.api.auth.mapper.UserMapper;
import dev.yukitrail.api.auth.model.AuthSession;
import dev.yukitrail.api.auth.model.UserAccount;
import dev.yukitrail.api.common.error.ApiException;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final AuthSessionMapper authSessionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final AuthProperties properties;
    private final Clock clock;
    private final String dummyPasswordHash;

    public AuthService(
            UserMapper userMapper,
            AuthSessionMapper authSessionMapper,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService,
            AuthProperties properties,
            Clock clock
    ) {
        this.userMapper = userMapper;
        this.authSessionMapper = authSessionMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.properties = properties;
        this.clock = clock;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public IssuedAuthSession register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userMapper.findByEmail(normalizedEmail) != null) {
            throw emailAlreadyRegistered();
        }

        UserAccount user = new UserAccount();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());

        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw emailAlreadyRegistered();
        }

        return issueSession(user);
    }

    @Transactional
    public IssuedAuthSession login(LoginRequest request) {
        UserAccount user = userMapper.findByEmail(normalizeEmail(request.email()));
        String passwordHash = user == null ? dummyPasswordHash : user.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);

        if (user == null || !passwordMatches) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS",
                    "Email or password is incorrect"
            );
        }

        return issueSession(user);
    }

    @Transactional
    public IssuedAuthSession refresh(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshSessionException();
        }

        LocalDateTime now = now();
        AuthSession session = authSessionMapper.findByTokenHashForUpdate(
                refreshTokenService.hash(rawRefreshToken)
        );

        if (session == null || session.getRevokedAt() != null || !session.getExpiresAt().isAfter(now)) {
            throw new InvalidRefreshSessionException();
        }

        if (authSessionMapper.revokeIfActive(session.getId(), now, now) != 1) {
            throw new InvalidRefreshSessionException();
        }

        UserAccount user = userMapper.findById(session.getUserId());
        if (user == null) {
            throw new InvalidRefreshSessionException();
        }

        return issueSession(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        AuthSession session = authSessionMapper.findByTokenHashForUpdate(
                refreshTokenService.hash(rawRefreshToken)
        );
        if (session != null && session.getRevokedAt() == null) {
            LocalDateTime now = now();
            authSessionMapper.revokeIfActive(session.getId(), now, now);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(String subject) {
        long userId;
        try {
            userId = Long.parseLong(subject);
        } catch (NumberFormatException exception) {
            throw unauthorized();
        }

        UserAccount user = userMapper.findById(userId);
        if (user == null) {
            throw unauthorized();
        }
        return UserResponse.from(user);
    }

    private IssuedAuthSession issueSession(UserAccount user) {
        String refreshToken = refreshTokenService.generate();
        AuthSession session = new AuthSession();
        session.setId(UUID.randomUUID().toString());
        session.setUserId(user.getId());
        session.setRefreshTokenHash(refreshTokenService.hash(refreshToken));
        session.setExpiresAt(now().plus(properties.getRefreshTokenTtl()));
        authSessionMapper.insert(session);

        JwtTokenService.AccessToken accessToken = jwtTokenService.issue(user.getId());
        AuthResponse response = new AuthResponse(
                accessToken.value(),
                "Bearer",
                accessToken.expiresIn(),
                UserResponse.from(user)
        );
        return new IssuedAuthSession(response, refreshToken);
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private static ApiException emailAlreadyRegistered() {
        return new ApiException(
                HttpStatus.CONFLICT,
                "EMAIL_ALREADY_REGISTERED",
                "Email is already registered"
        );
    }

    private static ApiException unauthorized() {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Authentication is required"
        );
    }

    public record IssuedAuthSession(AuthResponse response, String refreshToken) {
    }
}
