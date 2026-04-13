package org.jeecg.modules.taolink.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;

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

    @Schema(description = "低库存预警阈值（>0则覆盖全局默认）", example = "0")
    private Integer warningMin;

    @Schema(description = "积压天数阈值，>0则覆盖全局默认（默认30天）", example = "30")
    private Integer overstockDays;

    @Schema(description = "可用库存（计算字段）", example = "0")
    @TableField(exist = false)
    private Integer available;

    public Integer getAvailable() {
        int onHandVal = onHand == null ? 0 : onHand;
        int reservedVal = reserved == null ? 0 : reserved;
        return Math.max(0, onHandVal - reservedVal);
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    // Getters and Setters
    public String getWarehouseId() {
        return warehouseId;
    }

    public TaolinkInventory setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public String getProductSkuId() {
        return productSkuId;
    }

    public TaolinkInventory setProductSkuId(String productSkuId) {
        this.productSkuId = productSkuId;
        return this;
    }

    public Integer getOnHand() {
        return onHand;
    }

    public TaolinkInventory setOnHand(Integer onHand) {
        this.onHand = onHand;
        return this;
    }

    public Integer getReserved() {
        return reserved;
    }

    public TaolinkInventory setReserved(Integer reserved) {
        this.reserved = reserved;
        return this;
    }

    public Integer getWarningMin() {
        return warningMin;
    }

    public TaolinkInventory setWarningMin(Integer warningMin) {
        this.warningMin = warningMin;
        return this;
    }

    public Integer getOverstockDays() {
        return overstockDays;
    }

    public TaolinkInventory setOverstockDays(Integer overstockDays) {
        this.overstockDays = overstockDays;
        return this;
    }
}

