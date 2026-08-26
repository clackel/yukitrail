package dev.yukitrail.api.common.error;

import java.util.Map;

public record ValidationErrorData(Map<String, String> fields) {
}
