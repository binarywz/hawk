package binary.wz.voucher.controller;

import binary.wz.common.model.domain.ResultInfo;
import binary.wz.common.model.pojo.SeckillVoucher;
import binary.wz.common.util.ResultInfoUtil;
import binary.wz.voucher.service.VoucherService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author binarywz
 * @date 2021/9/1 23:22
 * @description: 秒杀控制层
 */
@RestController
@RequestMapping("voucher")
public class VoucherController {

    @Resource
    private VoucherService voucherService;
    @Resource
    private HttpServletRequest request;

    /**
     * 秒杀下单
     * @param voucherId
     * @param access_token
     * @return
     */
    @PostMapping("{voucherId}")
    public ResultInfo<String> doSeckill(@PathVariable Integer voucherId, String access_token) {
        ResultInfo resultInfo = voucherService.doSeckill(voucherId, access_token, request.getServletPath());
        return resultInfo;
    }

    /**
     * 新增秒杀活动
     * @param seckillVoucher
     * @return
     */
    @PostMapping("add")
    public ResultInfo<String> addSeckillVouchers(@RequestBody SeckillVoucher seckillVoucher) {
        voucherService.addSeckillVoucher(seckillVoucher);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),
                "添加成功");
    }

}
