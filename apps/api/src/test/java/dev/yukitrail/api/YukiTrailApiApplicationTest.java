package dev.yukitrail.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dev.yukitrail.api.auth.error.InvalidRefreshSessionException;
import dev.yukitrail.api.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class YukiTrailApiApplicationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @Autowired
    private JdbcClient jdbcClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void clearAuthData() {
        jdbcClient.sql("DELETE FROM auth_sessions").update();
        jdbcClient.sql("DELETE FROM users").update();
    }

    @Test
    void startsAndAppliesTheInitialSchema() {
        Integer migrationCount = jdbcClient.sql("""
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = DATABASE()
                          AND table_name IN (
                            'users',
                            'auth_sessions',
                            'trips',
                            'trip_days',
                            'itinerary_items'
                          )
                        """)
                .query(Integer.class)
                .single();

        assertThat(migrationCount).isEqualTo(5);
    }

    @Test
    void registersNormalizesEmailAndAuthenticatesTheCurrentUser() throws Exception {
        Registration registration = register(" Traveler@Example.com ");

        assertThat(registration.refreshCookie().isHttpOnly()).isTrue();
        assertThat(registration.setCookieHeader()).contains("SameSite=Lax");
        String savedEmail = jdbcClient.sql("SELECT email FROM users")
                .query(String.class)
                .single();
        String savedPasswordHash = jdbcClient.sql("SELECT password_hash FROM users")
                .query(String.class)
                .single();
        assertThat(savedEmail).isEqualTo("traveler@example.com");
        assertThat(savedPasswordHash).doesNotContain("correct-horse");

        mockMvc.perform(get("/api/v1/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + registration.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("traveler@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("雪路"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("TRAVELER@example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void loginFailureDoesNotRevealWhetherTheEmailExists() throws Exception {
        register("known@example.com");

        for (String body : List.of(
                loginJson("known@example.com", "wrong-password"),
                loginJson("missing@example.com", "wrong-password")
        )) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
<<<<<<< HEAD
                    .andExpect(jsonPath("$.message").value("邮箱或密码错误"));
=======
                    .andExpect(jsonPath("$.message").value("Email or password is incorrect"));
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
        }
    }

    @Test
    void refreshRotatesTheSessionAndRejectsTheOldToken() throws Exception {
        Registration registration = register("rotate@example.com");

        MvcResult refreshed = mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(registration.refreshCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();
        Cookie rotatedCookie = refreshed.getResponse().getCookie("yukitrail_refresh");

        assertThat(rotatedCookie).isNotNull();
        assertThat(rotatedCookie.getValue()).isNotEqualTo(registration.refreshCookie().getValue());
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(registration.refreshCookie()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_SESSION"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        Integer activeSessions = jdbcClient.sql("""
                        SELECT COUNT(*) FROM auth_sessions
                        WHERE revoked_at IS NULL AND expires_at > UTC_TIMESTAMP(6)
                        """)
                .query(Integer.class)
                .single();
        Integer revokedSessions = jdbcClient.sql("""
                        SELECT COUNT(*) FROM auth_sessions WHERE revoked_at IS NOT NULL
                        """)
                .query(Integer.class)
                .single();
        assertThat(activeSessions).isEqualTo(1);
        assertThat(revokedSessions).isEqualTo(1);
    }

    @Test
    void concurrentRefreshAllowsOnlyOneRequestToRotateTheSession() throws Exception {
        Registration registration = register("concurrent@example.com");
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> first = executor.submit(() -> refreshAfter(start, registration.refreshCookie().getValue()));
            Future<Boolean> second = executor.submit(() -> refreshAfter(start, registration.refreshCookie().getValue()));
            start.countDown();

            assertThat(List.of(first.get(), second.get())).containsExactlyInAnyOrder(true, false);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void logoutIsIdempotentAndPreventsFurtherRefresh() throws Exception {
        Registration registration = register("logout@example.com");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(registration.refreshCookie()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(registration.refreshCookie()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(registration.refreshCookie()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_SESSION"));
    }

    private boolean refreshAfter(CountDownLatch start, String refreshToken) throws InterruptedException {
        start.await();
        try {
            authService.refresh(refreshToken);
            return true;
        } catch (InvalidRefreshSessionException exception) {
            return false;
        }
    }

    private Registration register(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(900))
                .andReturn();

        Cookie refreshCookie = result.getResponse().getCookie("yukitrail_refresh");
        String accessToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("data")
                .get("accessToken")
                .asText();
        return new Registration(
                accessToken,
                refreshCookie,
                result.getResponse().getHeader(HttpHeaders.SET_COOKIE)
        );
    }

    private static String registerJson(String email) {
        return """
                {
                  "email": "%s",
                  "password": "correct-horse",
                  "nickname": "  雪路  "
                }
                """.formatted(email);
    }

    private static String loginJson(String email, String password) {
        return """
                {
                  "email": "%s",
                  "password": "%s"
                }
                """.formatted(email, password);
    }

    private record Registration(String accessToken, Cookie refreshCookie, String setCookieHeader) {
    }
}
