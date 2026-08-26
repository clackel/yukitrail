package dev.yukitrail.api.auth.mapper;

import dev.yukitrail.api.auth.model.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    UserAccount findById(@Param("id") long id);

    UserAccount findByEmail(@Param("email") String email);

    int insert(UserAccount user);
}
