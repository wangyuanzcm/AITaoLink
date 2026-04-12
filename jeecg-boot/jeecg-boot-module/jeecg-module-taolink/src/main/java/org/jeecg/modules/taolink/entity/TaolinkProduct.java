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
@Schema(description = "内部商品SPU")
@TableName("taolink_product")
public class TaolinkProduct extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "内部商品名")
    private String name;

    @Schema(description = "所属店铺ID（关联 taolink_shop）")
    private String shopId;

    @Schema(description = "类目ID（内部类目树节点）")
    private String categoryId;

    @Schema(description = "业务状态", example = "draft")
    private String status;

    @Schema(description = "上下架状态", example = "draft")
    private String listingStatus;

    @Schema(description = "上架时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date listedAt;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date delistedAt;
}

