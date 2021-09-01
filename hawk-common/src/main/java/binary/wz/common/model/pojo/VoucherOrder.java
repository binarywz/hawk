package binary.wz.common.model.pojo;

import binary.wz.common.model.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author binarywz
 * @date 2021/9/1 22:47
 * @description:
 */
@ApiModel(description = "代金券订单信息")
@Getter
@Setter
public class VoucherOrder extends BaseModel {

    @ApiModelProperty("订单编号")
    private String orderNo;
    @ApiModelProperty("代金券")
    private Integer fkVoucherId;
    @ApiModelProperty("下单用户")
    private Integer fkDinerId;
    @ApiModelProperty("生成qrcode")
    private String qrcode;
    @ApiModelProperty("支付方式 0=微信支付 1=支付宝")
    private int payment;
    @ApiModelProperty("订单状态 -1=已取消 0=未支付 1=已支付 2=已消费 3=已过期")
    private int status;
    @ApiModelProperty("订单类型 0=正常订单 1=抢购订单")
    private int orderType;
    @ApiModelProperty("抢购订单的外键")
    private int fkSeckillId;

}