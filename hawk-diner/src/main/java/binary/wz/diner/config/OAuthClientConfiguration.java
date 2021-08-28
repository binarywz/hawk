package binary.wz.diner.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author binarywz
 * @date 2021/8/28 16:30
 * @description: 客户端配置类
 */
@Component
@ConfigurationProperties(prefix = "oauth2.client")
@Getter
@Setter
public class OAuthClientConfiguration {

    private String clientId;
    private String secret;
    private String grant_type; // POST表单字段，不使用驼峰
    private String scope;

}
