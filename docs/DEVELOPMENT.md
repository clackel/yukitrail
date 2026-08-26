# Local development

## Request flow

```text
Browser -> apps/web -> /api/v1 -> apps/api -> MyBatis -> MySQL
                    -> AMap JS API (map and POI only)
```

The AMap service is not the source of truth for itinerary data. Later itinerary features will save a place snapshot with the item so an existing plan can still render if upstream place data changes.

## Configuration

The committed values are local-only defaults or empty examples. Real deployment values are injected through the environment.

| Area | Variable | Purpose |
| --- | --- | --- |
| Web | `VITE_API_BASE_URL` | REST API prefix |
| Web | `VITE_AMAP_KEY` | Browser-side AMap key |
| Web | `VITE_AMAP_SECURITY_JS_CODE` | AMap browser security code |
| API | `DB_HOST`, `DB_PORT` | MySQL network location |
| API | `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | MySQL database and credentials |
| API | `CORS_ALLOWED_ORIGINS` | Comma-separated browser origins |
| API | `JWT_ISSUER`, `JWT_ACCESS_TOKEN_TTL` | JWT issuer and access-token lifetime |
| API | `AUTH_REFRESH_TOKEN_TTL`, `AUTH_COOKIE_SECURE` | Refresh-session lifetime and Cookie security |
| API | `JWT_KEYS_REQUIRED` | Require deployment-provided RSA keys when `true` |
| API | `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY` | Base64 PKCS#8 private and X.509 public RSA keys |

`apps/api/.env.example` lists the complete API variables, but Spring Boot does not automatically load that file. Put values in the current terminal or the IDE run configuration. Local development may omit RSA keys and use an in-memory pair; deployment must set `JWT_KEYS_REQUIRED=true` and provide stable keys.

## Database lifecycle

Flyway owns schema changes. Add a new numbered migration for every schema change; do not edit an applied migration after it has been shared.

The initial migration creates only MVP tables:

- `users`
- `auth_sessions`
- `trips`
- `trip_days`
- `itinerary_items`, including the place snapshot columns

There are intentionally no tables for community, media, AI recommendations, notifications, or route optimization.

## Security boundary

`GET /api/v1/health` and the register, login, refresh and logout endpoints are public. `GET /api/v1/auth/me` and future business APIs require a Bearer JWT. Basic authentication, form login and server-side HTTP sessions are disabled.

Access tokens live only in Pinia memory. The browser receives the refresh token only as an `HttpOnly`, `SameSite=Lax` Cookie scoped to `/api/v1/auth`; the database stores only its SHA-256 hash. A refresh rotates the session under a database row lock, so the same refresh token cannot succeed twice.

## 注释与提示语言

- 后端关键类、事务边界、安全策略和不直观的 SQL 必须使用中文注释说明“为什么这样设计”。
- 前端只为会话、路由守卫、请求拦截器等关键流程保留简短中文注释，避免解释直观模板代码。
- 项目自定义的前端提示、API `message`、校验信息和日志使用中文。
- 稳定错误码、HTTP/JWT 字段、类名和变量名继续使用英文，避免破坏程序契约。
