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
@Schema(description = "来源商品（1688/淘宝）")
@TableName("taolink_source_offer")
public class TaolinkSourceOffer extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "来源平台", example = "1688")
    private String platform;

    @Schema(description = "来源商品ID（num_iid）")
    private String numIid;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "详情页URL")
    private String detailUrl;

    @Schema(description = "卖家昵称")
    private String sellerNick;

    @Schema(description = "最低价（分）", example = "0")
    private Integer priceMin;

    @Schema(description = "最高价（分）", example = "0")
    private Integer priceMax;

    @Schema(description = "起批量（1688）", example = "0")
    private Integer minNum;

    @Schema(description = "发货地/所在地")
    private String location;

    @Schema(description = "风险等级", example = "low")
    private String riskLevel;

    @Schema(description = "原始回包JSON（字符串）")
    private String rawJson;

    @Schema(description = "采集时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date fetchedAt;
}

