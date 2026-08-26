package dev.yukitrail.api.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ApiResponseTest {

    @Test
    void includesTheRequestTraceId() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE, "trace-123");

        ApiResponse<String> response = ApiResponse.ok("ready", request);

        assertThat(response.code()).isEqualTo("OK");
        assertThat(response.message()).isEqualTo("success");
        assertThat(response.data()).isEqualTo("ready");
        assertThat(response.traceId()).isEqualTo("trace-123");
    }
}

