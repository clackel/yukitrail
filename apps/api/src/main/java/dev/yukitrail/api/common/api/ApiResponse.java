package dev.yukitrail.api.common.api;

import jakarta.servlet.http.HttpServletRequest;

public record ApiResponse<T>(
        String code,
        String message,
        T data,
        String traceId
) {

    public static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return new ApiResponse<>("OK", "success", data, TraceIdFilter.getTraceId(request));
    }

    public static ApiResponse<Void> error(
            String code,
            String message,
            HttpServletRequest request
    ) {
        return new ApiResponse<>(code, message, null, TraceIdFilter.getTraceId(request));
    }
}

