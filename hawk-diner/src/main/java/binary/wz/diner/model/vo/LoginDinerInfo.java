package binary.wz.diner.model.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author binarywz
 * @date 2021/8/28 16:42
 * @description:
 */
@Setter
@Getter
public class LoginDinerInfo implements Serializable {

    private String nickname;
    private String token;
    private String avatarUrl;

}
