package binary.wz.diner.service;

import binary.wz.common.constant.ApiConstant;
import binary.wz.common.model.domain.ResultInfo;
import binary.wz.common.model.dto.DinerDTO;
import binary.wz.common.model.pojo.Diner;
import binary.wz.common.util.AssertUtils;
import binary.wz.common.util.ResultInfoUtil;
import binary.wz.diner.config.OAuthClientConfiguration;
import binary.wz.diner.mapper.DinerMapper;
import binary.wz.diner.model.domain.OAuthDinerInfo;
import binary.wz.diner.model.vo.LoginDinerInfo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

/**
 * @author binarywz
 * @date 2021/8/28 16:34
 * @description: 食客服务业务逻辑层
 */
@Service
public class DinerService {

    @Resource
    private RestTemplate restTemplate;
    @Value("${service.name.hawk-oauth-server}")
    private String oauthServerName;
    @Resource
    private OAuthClientConfiguration oAuthClientConfiguration;
    @Resource
    private DinerMapper dinerMapper;
    @Resource
    private VerifyCodeService verifyCodeService;

    /**
     * 用户注册
     * @param dinersDTO
     * @param path
     * @return
     */
    public ResultInfo register(DinerDTO dinersDTO, String path) {
        // 1.参数非空校验
        String username = dinersDTO.getUsername();
        AssertUtils.isNotEmpty(username, "请输入用户名");
        String password = dinersDTO.getPassword();
        AssertUtils.isNotEmpty(password, "请输入密码");
        String phone = dinersDTO.getPhone();
        AssertUtils.isNotEmpty(phone, "请输入手机号");
        String verifyCode = dinersDTO.getVerifyCode();
        AssertUtils.isNotEmpty(verifyCode, "请输入验证码");
        // 2.校验验证码
        String code = verifyCodeService.getCodeByPhone(phone);
        AssertUtils.isNotEmpty(code, "验证码已过期，请重新发送"); // 验证是否过期
        AssertUtils.isTrue(!dinersDTO.getVerifyCode().equals(code), "验证码不一致，请重新输入"); // 验证码一致性校验
        // 3.校验用户名是否已注册
        Diner diner = dinerMapper.selectByUsername(username.trim());
        AssertUtils.isTrue(diner != null, "用户名已存在，请重新输入");
        // 4.注册
        dinersDTO.setPassword(DigestUtil.md5Hex(password.trim())); // 密码加密
        dinerMapper.save(dinersDTO);
        // 5.自动登录
        return signIn(username.trim(), password.trim(), path);
    }

    /**
     * 校验手机号是否已注册
     * @param phone
     */
    public void checkPhoneIsRegistered(String phone) {
        AssertUtils.isNotEmpty(phone, "手机号不能为空");
        Diner diners = dinerMapper.selectByPhone(phone);
        AssertUtils.isTrue(diners == null, "该手机号未注册");
        AssertUtils.isTrue(diners.getIsValid() == 0, "该用户已锁定，请先解锁");
    }

    /**
     * 登录
     * @param account  帐号：用户名或手机或邮箱
     * @param password 密码
     * @param path     请求路径
     * @return
     */
    public ResultInfo signIn(String account, String password, String path) {
        // 参数校验
        AssertUtils.isNotEmpty(account, "请输入登录帐号");
        AssertUtils.isNotEmpty(password, "请输入登录密码");
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 构建请求体（请求参数）
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("username", account);
        body.add("password", password);
        body.setAll(BeanUtil.beanToMap(oAuthClientConfiguration));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        // 设置 Authorization
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(oAuthClientConfiguration.getClientId(),
                oAuthClientConfiguration.getSecret()));
        // 发送请求
        ResponseEntity<ResultInfo> result =
                restTemplate.postForEntity(oauthServerName + "oauth/token", entity, ResultInfo.class);
        // 处理返回结果
        AssertUtils.isTrue(result.getStatusCode() != HttpStatus.OK, "登录失败");
        ResultInfo resultInfo = result.getBody();
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            // 登录失败
            resultInfo.setData(resultInfo.getMessage());
            return resultInfo;
        }
        // 这里的 Data 是一个 LinkedHashMap 转成了域对象 OAuthDinerInfo
        OAuthDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
                new OAuthDinerInfo(), false);
        // 根据业务需求返回视图对象
        LoginDinerInfo loginDinerInfo = new LoginDinerInfo();
        loginDinerInfo.setToken(dinerInfo.getAccessToken());
        loginDinerInfo.setAvatarUrl(dinerInfo.getAvatarUrl());
        loginDinerInfo.setNickname(dinerInfo.getNickname());
        return ResultInfoUtil.buildSuccess(path, loginDinerInfo);
    }

}
