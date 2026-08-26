package dev.yukitrail.api.common.api;

import jakarta.servlet.http.HttpServletRequest;

/**
 * API 统一响应结构。
 *
 * <p>业务代码使用稳定的 {@code code} 做判断，面向用户的 {@code message} 统一使用中文。</p>
 */
public record ApiResponse<T>(
        String code,
        String message,
        T data,
        String traceId
) {

    public static <T> ApiResponse<T> ok(T data, HttpServletRequest request) {
        return new ApiResponse<>("OK", "请求成功", data, TraceIdFilter.getTraceId(request));
    }

    public static ApiResponse<Void> error(
            String code,
            String message,
            HttpServletRequest request
    ) {
        return new ApiResponse<>(code, message, null, TraceIdFilter.getTraceId(request));
    }

    public static <T> ApiResponse<T> error(
            String code,
            String message,
            T data,
            HttpServletRequest request
    ) {
        return new ApiResponse<>(code, message, data, TraceIdFilter.getTraceId(request));
    }
}
