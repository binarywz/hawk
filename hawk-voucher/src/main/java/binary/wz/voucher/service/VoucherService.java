package binary.wz.voucher.service;

import binary.wz.common.constant.ApiConstant;
import binary.wz.common.model.domain.ResultInfo;
import binary.wz.common.model.pojo.SeckillVoucher;
import binary.wz.common.model.pojo.VoucherOrder;
import binary.wz.common.model.vo.SignInDinerInfo;
import binary.wz.common.util.AssertUtils;
import binary.wz.common.util.ResultInfoUtil;
import binary.wz.voucher.mapper.SeckillVoucherMapper;
import binary.wz.voucher.mapper.VoucherOrderMapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * @author binarywz
 * @date 2021/9/1 22:45
 * @description:
 */
@Service
public class VoucherService {

    @Resource
    private SeckillVoucherMapper seckillVoucherMapper;
    @Resource
    private VoucherOrderMapper voucherOrderMapper;
    @Value("${service.name.hawk-oauth-server}")
    private String oauthServerName;
    @Resource
    private RestTemplate restTemplate;

    /**
     * 抢购代金券
     * @param voucherId   代金券 ID
     * @param accessToken 登录token
     * @Para path 访问路径
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultInfo doSeckill(Integer voucherId, String accessToken, String path) {
        // 基本参数校验
        AssertUtils.isTrue(voucherId == null || voucherId < 0, "请选择需要抢购的代金券");
        AssertUtils.isNotEmpty(accessToken, "请登录");

        // 注释原始的 关系型数据库 的流程
        // 判断此代金券是否加入抢购
        SeckillVoucher seckillVouchers = seckillVoucherMapper.selectVoucher(voucherId);
        AssertUtils.isTrue(seckillVouchers == null, "该代金券并未有抢购活动");
        // 判断是否有效
        AssertUtils.isTrue(seckillVouchers.getIsValid() == 0, "该活动已结束");
        // 判断是否开始、结束
        Date now = new Date();
        AssertUtils.isTrue(now.before(seckillVouchers.getStartTime()), "该抢购还未开始");
        AssertUtils.isTrue(now.after(seckillVouchers.getEndTime()), "该抢购已结束");
        // 判断是否卖完
        AssertUtils.isTrue(seckillVouchers.getAmount() < 1, "该券已经卖完了");
        // 获取登录用户信息
        String url = oauthServerName + "user/me?access_token={accessToken}";
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, accessToken);
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            resultInfo.setPath(path);
            return resultInfo;
        }
        // 这里的data是一个LinkedHashMap，SignInDinerInfo
        SignInDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
                new SignInDinerInfo(), false);
        // 判断登录用户是否已抢到(一个用户针对这次活动只能买一次)
        VoucherOrder order = voucherOrderMapper.findDinerOrder(dinerInfo.getId(),
                seckillVouchers.getFkVoucherId());
        AssertUtils.isTrue(order != null, "该用户已抢到该代金券，无需再抢");

        // 注释原始的 关系型数据库 的流程
        // 扣库存
        int count = seckillVoucherMapper.stockDecrease(seckillVouchers.getId());
        AssertUtils.isTrue(count == 0, "该券已经卖完了");

        // 下单
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setFkDinerId(dinerInfo.getId());
        voucherOrder.setFkSeckillId(seckillVouchers.getId());
        voucherOrder.setFkVoucherId(seckillVouchers.getFkVoucherId());
        String orderNo = IdUtil.getSnowflake(1, 1).nextIdStr();
        voucherOrder.setOrderNo(orderNo);
        voucherOrder.setOrderType(1);
        voucherOrder.setStatus(0);
        count = voucherOrderMapper.save(voucherOrder);
        AssertUtils.isTrue(count == 0, "用户抢购失败");

        return ResultInfoUtil.buildSuccess(path, "抢购成功");
    }

    /**
     * 添加需要抢购的代金券
     * @param seckillVoucher
     */
    @Transactional(rollbackFor = Exception.class)
    public void addSeckillVoucher(SeckillVoucher seckillVoucher) {
        // 非空校验
        AssertUtils.isTrue(seckillVoucher.getFkVoucherId() == null, "请选择需要抢购的代金券");
        AssertUtils.isTrue(seckillVoucher.getAmount() == 0, "请输入抢购总数量");
        Date now = new Date();
        AssertUtils.isNotNull(seckillVoucher.getStartTime(), "请输入开始时间");
        // 生产环境下面一行代码需放行，这里注释方便测试
        // AssertUtil.isTrue(now.after(seckillVouchers.getStartTime()), "开始时间不能早于当前时间");
        AssertUtils.isNotNull(seckillVoucher.getEndTime(), "请输入结束时间");
        AssertUtils.isTrue(now.after(seckillVoucher.getEndTime()), "结束时间不能早于当前时间");
        AssertUtils.isTrue(seckillVoucher.getStartTime().after(seckillVoucher.getEndTime()), "开始时间不能晚于结束时间");

        // 验证数据库中是否已经存在该券的秒杀活动
        SeckillVoucher seckillVouchersFromDb = seckillVoucherMapper.selectVoucher(seckillVoucher.getFkVoucherId());
        AssertUtils.isTrue(seckillVouchersFromDb != null, "该券已经拥有了抢购活动");
        // 插入数据库
        seckillVoucherMapper.save(seckillVoucher);
    }

}
