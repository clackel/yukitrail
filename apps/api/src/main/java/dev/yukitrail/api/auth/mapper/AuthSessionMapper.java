package dev.yukitrail.api.auth.mapper;

import java.time.LocalDateTime;

import dev.yukitrail.api.auth.model.AuthSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthSessionMapper {

    AuthSession findByTokenHashForUpdate(@Param("refreshTokenHash") String refreshTokenHash);

    int insert(AuthSession session);

    int revokeIfActive(
            @Param("id") String id,
            @Param("revokedAt") LocalDateTime revokedAt,
            @Param("lastUsedAt") LocalDateTime lastUsedAt
    );
}
