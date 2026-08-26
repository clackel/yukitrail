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

JWT key variables will be added with the authentication milestone, when the exact signing implementation is present. No placeholder token endpoints are exposed in the scaffold.

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

Only `GET /api/v1/health` is public in the scaffold. Other routes require authentication, and later resource services must verify ownership by the authenticated user rather than trusting IDs from the URL.

