package binary.wz.common.exception;

import binary.wz.common.constant.ApiConstant;
import lombok.Getter;
import lombok.Setter;

/**
 * @author binarywz
 * @date 2021/8/22 0:15
 * @description: 全局异常类
 */
@Getter
@Setter
public class ParameterException extends RuntimeException {

    private Integer errorCode;

    public ParameterException() {
        super(ApiConstant.ERROR_MESSAGE);
        this.errorCode = ApiConstant.ERROR_CODE;
    }

    public ParameterException(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public ParameterException(String message) {
        super(message);
        this.errorCode = ApiConstant.ERROR_CODE;
    }

    public ParameterException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
