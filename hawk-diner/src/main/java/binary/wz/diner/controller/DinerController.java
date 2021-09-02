package binary.wz.diner.controller;

import binary.wz.common.model.domain.ResultInfo;
import binary.wz.common.model.dto.DinerDTO;
import binary.wz.common.util.ResultInfoUtil;
import binary.wz.diner.service.DinerService;
import binary.wz.diner.service.VerifyCodeService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author binarywz
 * @date 2021/8/28 16:56
 * @description: 食客服务控制层
 */
@RestController
@Api(tags = "食客相关接口")
@RequestMapping("/diner")
public class DinerController {
    @Resource
    private DinerService dinerService;
    @Resource
    private VerifyCodeService verifyCodeService;
    @Resource
    private HttpServletRequest request;

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @GetMapping("/verify-code/send")
    public ResultInfo send(String phone) {
        verifyCodeService.send(phone);
        return ResultInfoUtil.buildSuccess("发送成功", request.getServletPath());
    }

    /**
     * 注册
     * @param dinerDTO
     * @return
     */
    @PostMapping("/register")
    public ResultInfo register(@RequestBody DinerDTO dinerDTO) {
        return dinerService.register(dinerDTO, request.getServletPath());
    }

    /**
     * 校验手机号是否已注册
     * @param phone
     * @return
     */
    @GetMapping("/checkPhone")
    public ResultInfo checkPhone(String phone) {
        dinerService.checkPhoneIsRegistered(phone);
        return ResultInfoUtil.buildSuccess(request.getServletPath());
    }

    /**
     * 登录
     * @param account
     * @param password
     * @return
     */
    @GetMapping("/signin")
    public ResultInfo signIn(String account, String password) {
        return dinerService.signIn(account, password, request.getServletPath());
    }
}
