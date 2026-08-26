package dev.yukitrail.api.common.error;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.yukitrail.api.common.api.ApiResponse;
import dev.yukitrail.api.common.api.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

<<<<<<< HEAD
/** 将校验、业务和未知异常转换为统一 API 响应，避免向前端暴露内部堆栈。 */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiResponse<Void>> handleApiException(
            ApiException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(exception.getStatus())
                .body(ApiResponse.error(exception.getCode(), exception.getMessage(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<ValidationErrorData>> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(ApiResponse.error(
                "VALIDATION_ERROR",
<<<<<<< HEAD
                "请求参数校验失败",
=======
                "Request validation failed",
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
                new ValidationErrorData(fields),
                request
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(ApiResponse.error(
                "VALIDATION_ERROR",
<<<<<<< HEAD
                "请求体格式不正确",
=======
                "Request body is invalid",
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
                request
        ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
<<<<<<< HEAD
        log.error("发生未处理的 API 异常；traceId={}", TraceIdFilter.getTraceId(request), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(
                "INTERNAL_ERROR",
                "服务器暂时无法处理请求，请稍后重试",
=======
        log.error("Unhandled API error; traceId={}", TraceIdFilter.getTraceId(request), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
                request
        ));
    }
}
