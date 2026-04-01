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
@Schema(description = "采购单（代发/备货共用）")
@TableName("taolink_purchase")
public class TaolinkPurchase extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "采购模式", example = "dropship")
    private String mode;

    @Schema(description = "供应商平台", example = "1688")
    private String supplierPlatform;

    @Schema(description = "状态", example = "draft")
    private String status;

    @Schema(description = "预计发货时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedShipAt;

    @Schema(description = "预计到货时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expectedArriveAt;
}

