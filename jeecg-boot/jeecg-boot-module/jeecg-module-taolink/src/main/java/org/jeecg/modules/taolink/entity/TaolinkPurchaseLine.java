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

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "采购单行")
@TableName("taolink_purchase_line")
public class TaolinkPurchaseLine extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "采购单ID")
    private String purchaseId;

    @Schema(description = "关联店铺ID")
    private String shopId;

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

    @Schema(description = "1688采购订单号（供应商端）")
    private String sourceOrderId;

    @Schema(description = "供应商发货的物流公司")
    private String sourceTrackingCompany;

    @Schema(description = "供应商发货的运单号")
    private String sourceTrackingNo;

    @Schema(description = "供应商发货时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date shippedAt;

    @Schema(description = "运费（分）", example = "0")
    private Integer freightCost;

    @Schema(description = "总成本 = unit_cost*qty + freight_cost（分）", example = "0")
    private Integer totalCost;
}

