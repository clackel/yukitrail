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

<<<<<<< HEAD
/**
 * 账户与会话领域服务。
 *
 * <p>负责邮箱规范化、密码校验、刷新令牌轮换以及从 JWT subject 识别当前用户。</p>
 */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
@Service
public class AuthService {

    private final UserMapper userMapper;
    private final AuthSessionMapper authSessionMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final AuthProperties properties;
    private final Clock clock;
<<<<<<< HEAD
    /**
     * 用户不存在时仍执行一次等成本的密码校验，降低通过响应耗时探测账号是否存在的风险。
     */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
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

<<<<<<< HEAD
    /** 注册用户，并在同一事务内创建首个刷新会话。 */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
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

<<<<<<< HEAD
    /** 登录失败统一返回同一错误，不泄露邮箱是否已经注册。 */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
    @Transactional
    public IssuedAuthSession login(LoginRequest request) {
        UserAccount user = userMapper.findByEmail(normalizeEmail(request.email()));
        String passwordHash = user == null ? dummyPasswordHash : user.getPasswordHash();
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);

        if (user == null || !passwordMatches) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS",
<<<<<<< HEAD
                    "邮箱或密码错误"
=======
                    "Email or password is incorrect"
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
            );
        }

        return issueSession(user);
    }

<<<<<<< HEAD
    /**
     * 轮换刷新会话。
     *
     * <p>Mapper 使用 {@code SELECT ... FOR UPDATE} 锁定旧会话，因此并发刷新只有一个事务能成功。</p>
     */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
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

<<<<<<< HEAD
    /** 撤销当前刷新会话；重复调用或缺少令牌时保持幂等。 */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
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

<<<<<<< HEAD
    /** 使用已经通过签名验证的 JWT subject 查询当前用户。 */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
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
<<<<<<< HEAD
        // 数据库只保存摘要；原始刷新令牌仅写入 HttpOnly Cookie。
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
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
<<<<<<< HEAD
                "该邮箱已经注册"
=======
                "Email is already registered"
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
        );
    }

    private static ApiException unauthorized() {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
<<<<<<< HEAD
                "请先登录后再访问"
        );
    }

    /** Controller 内部使用的签发结果，原始刷新令牌不会进入 JSON 响应。 */
=======
                "Authentication is required"
        );
    }

>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
    public record IssuedAuthSession(AuthResponse response, String refreshToken) {
    }
}
