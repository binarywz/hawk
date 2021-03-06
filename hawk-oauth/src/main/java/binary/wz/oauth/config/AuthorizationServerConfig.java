package binary.wz.oauth.config;

import binary.wz.common.model.domain.SignInIdentity;
import binary.wz.oauth.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

/**
 * @author binarywz
 * @date 2021/8/22 11:19
 * @description: 授权服务配置
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    // RedisTokenSore
    @Resource
    private RedisTokenStore redisTokenStore;
    // 认证管理对象
    @Resource
    private AuthenticationManager authenticationManager;
    // 密码编码器
    @Resource
    private PasswordEncoder passwordEncoder;
    // 客户端配置类
    @Resource
    private ClientOAuthDataConfig clientOAuthDataConfig;
    // 登录校验
    @Resource
    UserService userService;

    /**
     * 配置令牌端点安全约束
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.tokenKeyAccess("permitAll()")    // 允许访问 token 的公钥，默认 /oauth/token_key 是受保护的
                .checkTokenAccess("permitAll()"); // 允许检查 token 的状态，默认 /oauth/check_token 是受保护的
    }

    /**
     * 客户端配置-授权模型
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory().withClient(clientOAuthDataConfig.getClientId())                        // 客户端标识 ID
                .secret(passwordEncoder.encode(clientOAuthDataConfig.getSecret()))                // 客户端安全码
                .authorizedGrantTypes(clientOAuthDataConfig.getGrantTypes())                      // 授权类型
                .accessTokenValiditySeconds(clientOAuthDataConfig.getTokenValidityTime())         // token 有效期
                .refreshTokenValiditySeconds(clientOAuthDataConfig.getRefreshTokenValidityTime()) // 刷新 token 的有效期
                .scopes(clientOAuthDataConfig.getScopes());                                       // 客户端访问范围
    }

    /**
     * 配置授权以及令牌的访问端点和令牌服务
     *
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // 认证器
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(userService)                  // 具体登录的方法
                .tokenStore(redisTokenStore)                      // token存储的方式: Redis
                .tokenEnhancer((accessToken, authentication) -> { // 令牌增强对象，增强返回的结果
                    // 获取登录用户的信息，然后设置
                    SignInIdentity signInIdentity = (SignInIdentity) authentication.getPrincipal();
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    map.put("nickname", signInIdentity.getNickname());
                    map.put("avatarUrl", signInIdentity.getAvatarUrl());
                    DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) accessToken;
                    token.setAdditionalInformation(map);
                    return token;
                });
    }
}
