package binary.wz.diner.mapper;

import binary.wz.common.model.dto.DinerDTO;
import binary.wz.common.model.pojo.Diner;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author binarywz
 * @date 2021/8/29 21:20
 * @description: Diner Mapper
 */
public interface DinerMapper {
    // 根据手机号查询食客信息
    @Select("select id, username, phone, email, is_valid " +
            " from t_diner where phone = #{phone}")
    Diner selectByPhone(@Param("phone") String phone);

    // 根据用户名查询食客信息
    @Select("select id, username, phone, email, is_valid " +
            " from t_diner where username = #{username}")
    Diner selectByUsername(@Param("username") String username);

    // 新增食客信息
    @Insert("insert into " +
            " t_diner (username, password, phone, roles, is_valid, create_date, update_date) " +
            " values (#{username}, #{password}, #{phone}, \"ROLE_USER\", 1, now(), now())")
    int save(DinerDTO dinersDTO);
}
