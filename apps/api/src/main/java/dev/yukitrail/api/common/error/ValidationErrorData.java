package dev.yukitrail.api.common.error;

import java.util.Map;

/** 字段级校验错误，键名与请求 JSON 字段保持一致。 */
public record ValidationErrorData(Map<String, String> fields) {
}
