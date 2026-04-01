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
@Schema(description = "库存流水")
@TableName("taolink_inventory_movement")
public class TaolinkInventoryMovement extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "仓库ID")
    private String warehouseId;

    @Schema(description = "内部SKU ID")
    private String productSkuId;

    @Schema(description = "类型", example = "reserve")
    private String type;

    @Schema(description = "数量（正负皆可）", example = "0")
    private Integer qty;

    @Schema(description = "关联类型", example = "order")
    private String refType;

    @Schema(description = "关联ID")
    private String refId;
}

