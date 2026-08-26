CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    email VARCHAR(191) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(80) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE auth_sessions (
    id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    refresh_token_hash CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    last_used_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_auth_sessions_refresh_token_hash UNIQUE (refresh_token_hash),
    CONSTRAINT fk_auth_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_auth_sessions_user_active (user_id, revoked_at, expires_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE trips (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    title VARCHAR(120) NOT NULL,
    destination VARCHAR(160) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_trips_date_range CHECK (end_date >= start_date),
    INDEX idx_trips_user_updated (user_id, updated_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE trip_days (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    trip_id BIGINT UNSIGNED NOT NULL,
    trip_date DATE NOT NULL,
    day_number INT UNSIGNED NOT NULL,
    notes VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_trip_days_trip FOREIGN KEY (trip_id) REFERENCES trips (id) ON DELETE CASCADE,
    CONSTRAINT uk_trip_days_date UNIQUE (trip_id, trip_date),
    CONSTRAINT uk_trip_days_number UNIQUE (trip_id, day_number),
    CONSTRAINT chk_trip_days_number CHECK (day_number >= 1)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE itinerary_items (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    trip_day_id BIGINT UNSIGNED NOT NULL,
    start_time TIME NULL,
    end_time TIME NULL,
    title VARCHAR(160) NOT NULL,
    notes TEXT NULL,
    sort_order INT UNSIGNED NOT NULL,
    place_provider VARCHAR(20) NULL,
    place_poi_id VARCHAR(100) NULL,
    place_name VARCHAR(200) NULL,
    place_address VARCHAR(500) NULL,
    longitude DECIMAL(10, 7) NULL,
    latitude DECIMAL(10, 7) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_itinerary_items_day FOREIGN KEY (trip_day_id) REFERENCES trip_days (id) ON DELETE CASCADE,
    CONSTRAINT chk_itinerary_items_time_range CHECK (
        start_time IS NULL OR end_time IS NULL OR end_time >= start_time
    ),
    CONSTRAINT chk_itinerary_items_coordinates CHECK (
        (longitude IS NULL AND latitude IS NULL)
        OR (longitude BETWEEN -180 AND 180 AND latitude BETWEEN -90 AND 90)
    ),
    INDEX idx_itinerary_items_day_order (trip_day_id, sort_order, id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

