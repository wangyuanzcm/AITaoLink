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
@Schema(description = "内部SKU")
@TableName("taolink_product_sku")
public class TaolinkProductSku extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "SPU ID")
    private String productId;

    @Schema(description = "规格JSON（字符串）")
    private String specJson;

    @Schema(description = "状态", example = "active")
    private String status;
}

