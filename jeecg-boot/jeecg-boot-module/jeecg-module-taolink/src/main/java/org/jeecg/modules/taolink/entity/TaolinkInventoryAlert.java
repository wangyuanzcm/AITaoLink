package org.jeecg.modules.taolink.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "库存预警记录")
@TableName("taolink_inventory_alert")
public class TaolinkInventoryAlert extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "商品SKU ID")
    private String productSkuId;

    @Schema(description = "预警类型：low_stock（低库存）/ overstock（积压）")
    private String alertType;

    @Schema(description = "预警阈值")
    private Integer threshold;

    @Schema(description = "当前值")
    private Integer currentValue;

    @Schema(description = "状态：pending（待处理）/ resolved（已解决）")
    private String status;

    @Schema(description = "预警消息")
    private String message;

    @Schema(description = "处理人")
    private String handler;

    @Schema(description = "处理时间")
    private java.util.Date handledAt;
}
