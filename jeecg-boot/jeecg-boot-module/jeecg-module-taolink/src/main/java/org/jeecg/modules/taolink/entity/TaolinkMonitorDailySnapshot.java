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

@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "店铺监控每日快照")
@TableName("taolink_monitor_daily_snapshot")
public class TaolinkMonitorDailySnapshot extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺ID")
    private String shopId;

    @Schema(description = "快照日期")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date snapshotDate;

    @Schema(description = "商品总数", example = "0")
    private Integer productCount;

    @Schema(description = "在售商品数", example = "0")
    private Integer listedCount;

    @Schema(description = "当日新增上架数", example = "0")
    private Integer newListedCount;

    @Schema(description = "当日下架数", example = "0")
    private Integer newDelistedCount;

    @Schema(description = "当日订单数", example = "0")
    private Integer orderCount;

    @Schema(description = "当日GMV（分）", example = "0")
    private Integer orderAmount;

    @Schema(description = "当日退款数", example = "0")
    private Integer refundCount;

    @Schema(description = "库存有货SKU数", example = "0")
    private Integer inventoryItemCount;

    @Schema(description = "附加数据（JSON字符串）")
    private String snapshotJson;

    // Getters and Setters
    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public Date getSnapshotDate() {
        return snapshotDate;
    }

    public void setSnapshotDate(Date snapshotDate) {
        this.snapshotDate = snapshotDate;
    }

    public Integer getProductCount() {
        return productCount;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public Integer getListedCount() {
        return listedCount;
    }

    public void setListedCount(Integer listedCount) {
        this.listedCount = listedCount;
    }

    public Integer getNewListedCount() {
        return newListedCount;
    }

    public void setNewListedCount(Integer newListedCount) {
        this.newListedCount = newListedCount;
    }

    public Integer getNewDelistedCount() {
        return newDelistedCount;
    }

    public void setNewDelistedCount(Integer newDelistedCount) {
        this.newDelistedCount = newDelistedCount;
    }

    public Integer getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Integer orderCount) {
        this.orderCount = orderCount;
    }

    public Integer getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(Integer orderAmount) {
        this.orderAmount = orderAmount;
    }

    public Integer getRefundCount() {
        return refundCount;
    }

    public void setRefundCount(Integer refundCount) {
        this.refundCount = refundCount;
    }

    public Integer getInventoryItemCount() {
        return inventoryItemCount;
    }

    public void setInventoryItemCount(Integer inventoryItemCount) {
        this.inventoryItemCount = inventoryItemCount;
    }

    public String getSnapshotJson() {
        return snapshotJson;
    }

    public void setSnapshotJson(String snapshotJson) {
        this.snapshotJson = snapshotJson;
    }
}
