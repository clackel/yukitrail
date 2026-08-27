package dev.yukitrail.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

/**
 * YukiTrail API 启动入口。
 *
 * <p>项目使用 Bearer JWT 鉴权，不启用 Spring Boot 自动生成的内存用户。</p>
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class YukiTrailApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(YukiTrailApiApplication.class, args);
    }
}
