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
@Data
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
}
