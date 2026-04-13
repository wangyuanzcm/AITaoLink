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

    // Getters and Setters
    public String getProductSkuId() {
        return productSkuId;
    }

    public TaolinkInventoryAlert setProductSkuId(String productSkuId) {
        this.productSkuId = productSkuId;
        return this;
    }

    public String getAlertType() {
        return alertType;
    }

    public TaolinkInventoryAlert setAlertType(String alertType) {
        this.alertType = alertType;
        return this;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public TaolinkInventoryAlert setThreshold(Integer threshold) {
        this.threshold = threshold;
        return this;
    }

    public Integer getCurrentValue() {
        return currentValue;
    }

    public TaolinkInventoryAlert setCurrentValue(Integer currentValue) {
        this.currentValue = currentValue;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public TaolinkInventoryAlert setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public TaolinkInventoryAlert setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getHandler() {
        return handler;
    }

    public TaolinkInventoryAlert setHandler(String handler) {
        this.handler = handler;
        return this;
    }

    public java.util.Date getHandledAt() {
        return handledAt;
    }

    public TaolinkInventoryAlert setHandledAt(java.util.Date handledAt) {
        this.handledAt = handledAt;
        return this;
    }
}
