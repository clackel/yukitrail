package dev.yukitrail.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class YukiTrailApiApplicationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer MYSQL = new MySQLContainer("mysql:8.4");

    @Autowired
    private JdbcClient jdbcClient;

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
}
