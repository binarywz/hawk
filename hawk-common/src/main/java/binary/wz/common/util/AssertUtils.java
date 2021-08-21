package binary.wz.common.util;

import binary.wz.common.constant.ApiConstant;
import binary.wz.common.exception.ParameterException;
import cn.hutool.core.util.StrUtil;

/**
 * @author binarywz
 * @date 2021/8/22 0:17
 * @description: 断言工具类
 */
public class AssertUtils {

    /**
     * 判断字符串非空
     *
     * @param str
     * @param message
     */
    public static void isNotEmpty(String str, String... message) {
        if (StrUtil.isBlank(str)) {
            execute(message);
        }
    }

    /**
     * 判断对象非空
     *
     * @param obj
     * @param message
     */
    public static void isNotNull(Object obj, String... message) {
        if (obj == null) {
            execute(message);
        }
    }

    /**
     * 判断结果是否为真
     *
     * @param isTrue
     * @param message
     */
    public static void isTrue(boolean isTrue, String... message) {
        if (isTrue) {
            execute(message);
        }
    }

    /**
     * 最终执行方法
     *
     * @param message
     */
    private static void execute(String... message) {
        String msg = ApiConstant.ERROR_MESSAGE;
        if (message != null && message.length > 0) {
            msg = message[0];
        }
        throw new ParameterException(msg);
    }

}
