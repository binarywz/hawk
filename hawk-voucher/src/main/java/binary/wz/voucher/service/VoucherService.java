package binary.wz.voucher.service;

import binary.wz.common.constant.ApiConstant;
import binary.wz.common.constant.RedisKeyConstant;
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
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

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
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private DefaultRedisScript defaultRedisScript;

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

        // 采用redis
        String key = RedisKeyConstant.SECKILL_VOUCHER.getKey() + voucherId;
        Map<String, Object> map = redisTemplate.opsForHash().entries(key);
        SeckillVoucher seckillVoucher = BeanUtil.mapToBean(map, SeckillVoucher.class, true, null);

        // 判断是否开始、结束
        Date now = new Date();
        AssertUtils.isTrue(now.before(seckillVoucher.getStartTime()), "该抢购还未开始");
        AssertUtils.isTrue(now.after(seckillVoucher.getEndTime()), "该抢购已结束");
        // 判断是否卖完
        AssertUtils.isTrue(seckillVoucher.getAmount() < 1, "该券已经卖完了");
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
                seckillVoucher.getFkVoucherId());
        AssertUtils.isTrue(order != null, "该用户已抢到该代金券，无需再抢");

        // 下单
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setFkDinerId(dinerInfo.getId());
        // voucherOrder.setFkSeckillId(seckillVoucher.getId()); // redis中不需要维护外键信息
        voucherOrder.setFkVoucherId(seckillVoucher.getFkVoucherId());
        String orderNo = IdUtil.getSnowflake(1, 1).nextIdStr();
        voucherOrder.setOrderNo(orderNo);
        voucherOrder.setOrderType(1);
        voucherOrder.setStatus(0);
        long count = voucherOrderMapper.save(voucherOrder);
        AssertUtils.isTrue(count == 0, "用户抢购失败");

        // redis+lua减库存
        // 下单后减库存可以保证数据库+redis两个层面的事务
        List<String> keys = new ArrayList<>();
        keys.add(key);
        keys.add("amount");
        Long amount = (Long) redisTemplate.execute(defaultRedisScript, keys);
        AssertUtils.isTrue(amount == null || amount < 1, "该用户已抢到该代金券，无需再抢");

        // TODO 库存持久化处理

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

        // 采用redis实现
        String key = RedisKeyConstant.SECKILL_VOUCHER.getKey() +
                seckillVoucher.getFkVoucherId();
        // 验证redis中是否已经存在该券的秒杀活动
        // 为什么使用hash: 1.hash可以很方便地获取库存字段 2.hash在Redis中存储的时候不会进行序列化与反序列，可以提高性能
        Map<String, Object> map = redisTemplate.opsForHash().entries(key);
        AssertUtils.isTrue(!map.isEmpty() && (int)map.get("amount") > 0, "该券已经拥有了抢购活动");

        // redis
        seckillVoucher.setIsValid(1);
        seckillVoucher.setCreateDate(now);
        seckillVoucher.setUpdateDate(now);
        redisTemplate.opsForHash().putAll(key, BeanUtil.beanToMap(seckillVoucher));

        // TODO 持久化处理
    }

}
