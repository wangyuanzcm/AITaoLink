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
@Schema(description = "SKU绑定（内部SKU↔来源SKU）")
@TableName("taolink_sku_binding")
public class TaolinkSkuBinding extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "内部SKU ID")
    private String productSkuId;

    @Schema(description = "来源商品ID")
    private String sourceOfferId;

    @Schema(description = "来源SKU ID")
    private String sourceSkuId;

    @Schema(description = "来源规格properties（如 1627207:1347647754）")
    private String sourceProperties;

    @Schema(description = "成本价（分）", example = "0")
    private Integer costPrice;

    @Schema(description = "是否主绑定", example = "true")
    private Boolean isPrimary;
}

