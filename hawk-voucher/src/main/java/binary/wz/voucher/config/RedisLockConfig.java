package binary.wz.voucher.config;

import binary.wz.voucher.component.RedisLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * @author binarywz
 * @date 2021/9/5 10:55
 * @description: redis分布式锁配置类
 */
@Configuration
public class RedisLockConfig {
    @Resource
    private RedisTemplate redisTemplate;

    @Bean
    public RedisLock redisLock() {
        RedisLock redisLock = new RedisLock(redisTemplate);
        return redisLock;
    }
}
