package binary.wz.diner.controller;

import binary.wz.common.model.domain.ResultInfo;
import binary.wz.common.util.ResultInfoUtil;
import binary.wz.diner.service.VerifyCodeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author binarywz
 * @date 2021/8/29 10:13
 * @description: 验证码控制逻辑层
 */
@RestController
@RequestMapping(("verify-code"))
public class VerifyCodeController {
    @Resource
    private VerifyCodeService verifyCodeService;
    @Resource
    private HttpServletRequest request;

    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @GetMapping("send")
    public ResultInfo send(String phone) {
        verifyCodeService.send(phone);
        return ResultInfoUtil.buildSuccess("发送成功", request.getServletPath());
    }
}
