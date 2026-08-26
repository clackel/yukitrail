package dev.yukitrail.api.auth.mapper;

import dev.yukitrail.api.auth.model.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 用户账户的 MyBatis 数据访问接口，SQL 位于对应 XML Mapper。 */
@Mapper
public interface UserMapper {

    UserAccount findById(@Param("id") long id);

    UserAccount findByEmail(@Param("email") String email);

    int insert(UserAccount user);
}
