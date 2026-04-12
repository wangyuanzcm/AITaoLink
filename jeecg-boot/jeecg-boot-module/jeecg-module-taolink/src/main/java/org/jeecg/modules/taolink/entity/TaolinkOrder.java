package org.jeecg.modules.taolink.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "订单（淘宝同步）")
@TableName("taolink_order")
public class TaolinkOrder extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺ID（关联 taolink_shop）")
    private String shopId;

    @Schema(description = "平台订单号（淘宝）")
    private String platformOrderId;

    @Schema(description = "订单状态", example = "created")
    private String status;

    @Schema(description = "支付时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    @Schema(description = "收货信息快照（脱敏JSON字符串）")
    private String receiverSnapshotJson;

    @Schema(description = "淘宝原始回包JSON（字符串）")
    private String rawJson;
}

