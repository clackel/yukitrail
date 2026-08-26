package dev.yukitrail.api.auth.error;

import dev.yukitrail.api.common.error.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidRefreshSessionException extends ApiException {

    public InvalidRefreshSessionException() {
        super(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_SESSION", "Refresh session is invalid or expired");
    }
}
