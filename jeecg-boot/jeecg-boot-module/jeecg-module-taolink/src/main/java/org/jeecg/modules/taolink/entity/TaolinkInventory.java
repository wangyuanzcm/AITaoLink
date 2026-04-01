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
@Schema(description = "库存台账")
@TableName("taolink_inventory")
public class TaolinkInventory extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "仓库ID")
    private String warehouseId;

    @Schema(description = "内部SKU ID")
    private String productSkuId;

    @Schema(description = "现有库存", example = "0")
    private Integer onHand;

    @Schema(description = "预占库存", example = "0")
    private Integer reserved;

    @TableField(exist = false)
    @Schema(description = "可用库存（计算字段）", example = "0")
    public Integer getAvailable() {
        int onHandVal = onHand == null ? 0 : onHand;
        int reservedVal = reserved == null ? 0 : reserved;
        return Math.max(0, onHandVal - reservedVal);
    }
}

