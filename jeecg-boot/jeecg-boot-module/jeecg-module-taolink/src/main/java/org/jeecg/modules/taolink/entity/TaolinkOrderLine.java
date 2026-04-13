package org.jeecg.modules.taolink.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "订单行")
@TableName("taolink_order_line")
public class TaolinkOrderLine extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "内部SKU ID")
    private String productSkuId;

    @Schema(description = "购买数量", example = "1")
    private Integer qty;

    @Schema(description = "成交价（分）", example = "0")
    private Integer salePrice;

    @Schema(description = "履约模式", example = "stock")
    private String fulfillmentMode;

    @Schema(description = "履约状态", example = "unassigned")
    private String fulfillmentStatus;

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public TaolinkOrderLine setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getProductSkuId() {
        return productSkuId;
    }

    public TaolinkOrderLine setProductSkuId(String productSkuId) {
        this.productSkuId = productSkuId;
        return this;
    }

    public Integer getQty() {
        return qty;
    }

    public TaolinkOrderLine setQty(Integer qty) {
        this.qty = qty;
        return this;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public TaolinkOrderLine setSalePrice(Integer salePrice) {
        this.salePrice = salePrice;
        return this;
    }

    public String getFulfillmentMode() {
        return fulfillmentMode;
    }

    public TaolinkOrderLine setFulfillmentMode(String fulfillmentMode) {
        this.fulfillmentMode = fulfillmentMode;
        return this;
    }

    public String getFulfillmentStatus() {
        return fulfillmentStatus;
    }

    public TaolinkOrderLine setFulfillmentStatus(String fulfillmentStatus) {
        this.fulfillmentStatus = fulfillmentStatus;
        return this;
    }
}

