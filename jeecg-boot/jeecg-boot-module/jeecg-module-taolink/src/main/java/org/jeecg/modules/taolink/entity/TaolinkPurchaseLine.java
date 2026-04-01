package org.jeecg.modules.taolink.entity;

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
@Schema(description = "采购单行")
@TableName("taolink_purchase_line")
public class TaolinkPurchaseLine extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "采购单ID")
    private String purchaseId;

    @Schema(description = "来源商品ID")
    private String sourceOfferId;

    @Schema(description = "来源SKU ID")
    private String sourceSkuId;

    @Schema(description = "数量", example = "0")
    private Integer qty;

    @Schema(description = "单价成本（分）", example = "0")
    private Integer unitCost;

    @Schema(description = "规格快照JSON（字符串）")
    private String specSnapshotJson;
}

