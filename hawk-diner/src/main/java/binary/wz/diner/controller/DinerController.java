package binary.wz.diner.controller;

import binary.wz.common.model.domain.ResultInfo;
import binary.wz.diner.service.DinerService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author binarywz
 * @date 2021/8/28 16:56
 * @description: 食客服务控制层
 */
@RestController
@Api(tags = "食客相关接口")
public class DinerController {
    @Resource
    private DinerService dinerService;

    @Resource
    private HttpServletRequest request;

    /**
     * 登录
     * @param account
     * @param password
     * @return
     */
    @GetMapping("signin")
    public ResultInfo signIn(String account, String password) {
        return dinerService.signIn(account, password, request.getServletPath());
    }
}
