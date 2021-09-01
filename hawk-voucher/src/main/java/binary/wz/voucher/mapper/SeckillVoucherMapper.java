package binary.wz.voucher.mapper;

import binary.wz.common.model.pojo.SeckillVoucher;
import org.apache.ibatis.annotations.*;

/**
 * @author binarywz
 * @date 2021/9/1 22:42
 * @description: 秒杀代金券Mapper
 */
public interface SeckillVoucherMapper {

    // 新增秒杀活动
    @Insert("insert into t_seckill_voucher (fk_voucher_id, amount, start_time, end_time, is_valid, create_date, update_date) " +
            " values (#{fkVoucherId}, #{amount}, #{startTime}, #{endTime}, 1, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(SeckillVoucher seckillVouchers);

    // 根据代金券 ID 查询该代金券是否参与抢购活动
    @Select("select id, fk_voucher_id, amount, start_time, end_time, is_valid " +
            " from t_seckill_voucher where fk_voucher_id = #{voucherId}")
    SeckillVoucher selectVoucher(Integer voucherId);

    // 减库存
    @Update("update t_seckill_voucher set amount = amount - 1 " +
            " where id = #{seckillId}")
    int stockDecrease(@Param("seckillId") int seckillId);

}
