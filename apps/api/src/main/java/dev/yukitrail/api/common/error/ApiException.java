package dev.yukitrail.api.common.error;

import org.springframework.http.HttpStatus;

<<<<<<< HEAD
/** 可安全返回给前端的业务异常，携带 HTTP 状态和稳定错误码。 */
=======
>>>>>>> 407e499645ce58ce24342b295addb82bf5271147
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
