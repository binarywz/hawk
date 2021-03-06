package binary.wz.common.model.pojo;

import binary.wz.common.model.base.BaseModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author binarywz
 * @date 2021/9/1 22:06
 * @description:
 */
@Setter
@Getter
@ApiModel(description = "抢购代金券信息")
public class SeckillVoucher extends BaseModel {

    @ApiModelProperty("代金券外键")
    private Integer fkVoucherId;
    @ApiModelProperty("数量")
    private int amount;
    @ApiModelProperty("抢购开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date startTime;
    @ApiModelProperty("抢购结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date endTime;

}
