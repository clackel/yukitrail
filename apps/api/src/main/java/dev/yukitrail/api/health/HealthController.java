package dev.yukitrail.api.health;

import dev.yukitrail.api.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 提供无需认证的轻量健康检查，用于本地联调和部署探针。 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ApiResponse<HealthData> health(HttpServletRequest request) {
        return ApiResponse.ok(new HealthData("yukitrail-api", "UP"), request);
    }

    public record HealthData(String service, String status) {
    }
}
