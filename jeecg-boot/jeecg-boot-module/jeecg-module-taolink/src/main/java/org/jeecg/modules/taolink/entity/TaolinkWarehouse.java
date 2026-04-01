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
@Schema(description = "仓库")
@TableName("taolink_warehouse")
public class TaolinkWarehouse extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "仓库名称")
    private String name;

    @Schema(description = "仓库类型", example = "self")
    private String type;
}

