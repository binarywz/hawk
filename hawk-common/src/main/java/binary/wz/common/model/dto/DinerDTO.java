package binary.wz.common.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author binarywz
 * @date 2021/8/29 21:23
 * @description:
 */
@Getter
@Setter
@ApiModel(description = "注册用户信息")
public class DinerDTO implements Serializable {

    @ApiModelProperty("用户名")
    private String username;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("手机号")
    private String phone;
    @ApiModelProperty("验证码")
    private String verifyCode;

}
