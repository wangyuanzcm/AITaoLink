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
@Schema(description = "内部商品SPU")
@TableName("taolink_product")
public class TaolinkProduct extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "内部商品名")
    private String name;

    @Schema(description = "类目ID（内部类目树节点）")
    private String categoryId;

    @Schema(description = "状态", example = "draft")
    private String status;
}

