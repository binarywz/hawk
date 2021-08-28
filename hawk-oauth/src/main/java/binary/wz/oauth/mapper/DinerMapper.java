package binary.wz.oauth.mapper;

import binary.wz.common.model.pojo.Diner;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author binarywz
 * @date 2021/8/22 16:45
 * @description: DinerMapper
 */
public interface DinerMapper {

    // 根据用户名 or 手机号 or 邮箱查询用户信息
    @Select("select id, username, nickname, phone, email, " +
            "password, avatar_url, roles, is_valid from t_diners where " +
            "(username = #{account} or phone = #{account} or email = #{account})")
    Diner selectByAccountInfo(@Param("account") String account);

}
