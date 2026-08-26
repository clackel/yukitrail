package dev.yukitrail.api.auth.mapper;

import java.time.LocalDateTime;

import dev.yukitrail.api.auth.model.AuthSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 刷新会话的 MyBatis 数据访问接口。 */
@Mapper
public interface AuthSessionMapper {

    /** 查询并锁定刷新会话，保证令牌轮换不能并发成功两次。 */
    AuthSession findByTokenHashForUpdate(@Param("refreshTokenHash") String refreshTokenHash);

    int insert(AuthSession session);

    /** 仅撤销仍处于有效状态的记录，返回值用于判断并发竞争结果。 */
    int revokeIfActive(
            @Param("id") String id,
            @Param("revokedAt") LocalDateTime revokedAt,
            @Param("lastUsedAt") LocalDateTime lastUsedAt
    );
}
