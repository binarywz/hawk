package binary.wz.common.model.pojo;

import binary.wz.common.model.base.BaseModel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author binarywz
 * @date 2021/8/22 16:49
 * @description: Diner实体类
 */
@Getter
@Setter
public class Diner extends BaseModel {
    // 主键
    private Integer id;
    // 用户名
    private String username;
    // 昵称
    private String nickname;
    // 密码
    private String password;
    // 手机号
    private String phone;
    // 邮箱
    private String email;
    // 头像
    private String avatarUrl;
    // 角色
    private String roles;
}
