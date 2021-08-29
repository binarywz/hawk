package binary.wz.common.constant;

import lombok.Getter;

/**
 * @author binarywz
 * @date 2021/8/29 10:08
 * @description: Redis公共枚举类
 */
@Getter
public enum RedisKeyConstant {
    VERIFY_CODE("VERIFY_CODE:", "验证码"),
    SECKKILL_VOUCHERS("SECKKILL_VOUCHERS:", "秒杀券的key"),
    LOCK_KEY("LOCK_KEY:", "分布式锁的key");

    private String key;
    private String desc;

    RedisKeyConstant(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }
}
