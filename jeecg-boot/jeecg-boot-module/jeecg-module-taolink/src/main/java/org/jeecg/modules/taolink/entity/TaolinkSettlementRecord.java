package org.jeecg.modules.taolink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 代发结算流水记录
 */
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("taolink_settlement_record")
public class TaolinkSettlementRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 店铺ID
     */
    private String shopId;

    /**
     * 结算类型：purchase（采购）、freight（运费）
     */
    private String settleType;

    /**
     * 结算金额
     */
    private BigDecimal amount;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 供应商ID
     */
    private String supplierId;

    /**
     * 关联采购单ID
     */
    private String purchaseId;

    /**
     * 关联采购单行ID
     */
    private String purchaseLineId;

    /**
     * 关联订单ID
     */
    private String orderId;

    /**
     * 状态：pending（待结算）、settled（已结算）、cancelled（已取消）
     */
    private String status;

    /**
     * 结算时间
     */
    private Date settleTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public TaolinkSettlementRecord setId(String id) {
        this.id = id;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public TaolinkSettlementRecord setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public String getShopId() {
        return shopId;
    }

    public TaolinkSettlementRecord setShopId(String shopId) {
        this.shopId = shopId;
        return this;
    }

    public String getSettleType() {
        return settleType;
    }

    public TaolinkSettlementRecord setSettleType(String settleType) {
        this.settleType = settleType;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TaolinkSettlementRecord setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public TaolinkSettlementRecord setSupplierName(String supplierName) {
        this.supplierName = supplierName;
        return this;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public TaolinkSettlementRecord setSupplierId(String supplierId) {
        this.supplierId = supplierId;
        return this;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public TaolinkSettlementRecord setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
        return this;
    }

    public String getPurchaseLineId() {
        return purchaseLineId;
    }

    public TaolinkSettlementRecord setPurchaseLineId(String purchaseLineId) {
        this.purchaseLineId = purchaseLineId;
        return this;
    }

    public String getOrderId() {
        return orderId;
    }

    public TaolinkSettlementRecord setOrderId(String orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public TaolinkSettlementRecord setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getSettleTime() {
        return settleTime;
    }

    public TaolinkSettlementRecord setSettleTime(Date settleTime) {
        this.settleTime = settleTime;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public TaolinkSettlementRecord setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public String getCreateBy() {
        return createBy;
    }

    public TaolinkSettlementRecord setCreateBy(String createBy) {
        this.createBy = createBy;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public TaolinkSettlementRecord setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public TaolinkSettlementRecord setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public TaolinkSettlementRecord setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }
}
