package dev.yukitrail.api.auth.error;

import dev.yukitrail.api.common.error.ApiException;
import org.springframework.http.HttpStatus;

/** 刷新 Cookie 缺失、过期、已撤销或被重复使用时抛出。 */
public class InvalidRefreshSessionException extends ApiException {

    public InvalidRefreshSessionException() {
        super(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_SESSION", "刷新会话无效或已过期，请重新登录");
    }
}
