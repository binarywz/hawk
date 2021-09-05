package binary.wz.voucher.service;

import binary.wz.common.constant.ApiConstant;
import binary.wz.common.constant.RedisKeyConstant;
import binary.wz.common.exception.ParameterException;
import binary.wz.common.model.domain.ResultInfo;
import binary.wz.common.model.pojo.SeckillVoucher;
import binary.wz.common.model.pojo.VoucherOrder;
import binary.wz.common.model.vo.SignInDinerInfo;
import binary.wz.common.util.AssertUtils;
import binary.wz.common.util.ResultInfoUtil;
import binary.wz.voucher.component.RedisLock;
import binary.wz.voucher.mapper.SeckillVoucherMapper;
import binary.wz.voucher.mapper.VoucherOrderMapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
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
    @Resource
    private RedisLock redisLock;

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

        // 分布式锁保证同一活动一个账号限购一次
        String lockName = RedisKeyConstant.LOCK_KEY.getKey() + dinerInfo.getId()
                            + ":" + voucherId;
        long expireTime = seckillVoucher.getEndTime().getTime() - now.getTime();
        String lockKey = redisLock.tryLock(lockName, expireTime); // 锁失效时间，视频中通过失效时间限制一人一单

        // 如果没获取到锁表明已经有线程处于购买过程中
        if (!StringUtils.isNotBlank(lockKey)) {
            return ResultInfoUtil.buildSuccess(path, "抢购人数太多，请稍后再试！");
        }

        try {
            // 在获取到锁之后再判断用户是否抢过，不然存在释放锁之后其他用户重复下单的情况
            // 判断登录用户是否已抢到(一个用户针对这次活动只能买一次)
            VoucherOrder order = voucherOrderMapper.findDinerOrder(dinerInfo.getId(),
                    seckillVoucher.getFkVoucherId());
            if (order != null) {
                return ResultInfoUtil.buildSuccess(path, "您已经抢到了优惠券！");
            }

            // redis+lua减库存，没有限购次数可以不使用分布式锁
            List<String> keys = new ArrayList<>();
            keys.add(key);
            keys.add("amount");
            Long amount = (Long) redisTemplate.execute(defaultRedisScript, keys);
            AssertUtils.isTrue(amount == null || amount < 0, "优惠券已经抢光了！");

            // 下单
            VoucherOrder voucherOrder = new VoucherOrder();
            voucherOrder.setFkDinerId(dinerInfo.getId());
            // voucherOrder.setFkSeckillId(seckillVoucher.getId()); // TODO 需要在redis中添加秒杀活动id
            voucherOrder.setFkVoucherId(seckillVoucher.getFkVoucherId());
            String orderNo = IdUtil.getSnowflake(1, 1).nextIdStr();
            voucherOrder.setOrderNo(orderNo);
            voucherOrder.setOrderType(1);
            voucherOrder.setStatus(0);
            long count = voucherOrderMapper.save(voucherOrder);
            AssertUtils.isTrue(count == 0, "用户抢购失败");

            // TODO 库存持久化处理
        } catch (Exception ex) {
            // 手动回滚事务
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 解锁
            redisLock.unlock(lockName, lockKey);
            // TODO 发生异常时需要回滚redis库存
            if (ex instanceof ParameterException) {
                return ResultInfoUtil.buildError(0, "优惠券已经抢光了！", path);
            }
        } finally {
            redisLock.unlock(lockName, lockKey);
        }

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

        // TODO 持久化处理
        // 秒杀活动存入redis之前进行持久化可以获取秒杀活动的id，更容易维护

        // redis
        seckillVoucher.setIsValid(1);
        seckillVoucher.setCreateDate(now);
        seckillVoucher.setUpdateDate(now);
        redisTemplate.opsForHash().putAll(key, BeanUtil.beanToMap(seckillVoucher));
    }

}
