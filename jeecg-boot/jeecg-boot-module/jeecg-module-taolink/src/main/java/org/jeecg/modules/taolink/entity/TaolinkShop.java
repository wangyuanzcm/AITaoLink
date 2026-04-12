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
@Schema(description = "淘宝店铺绑定")
@TableName("taolink_shop")
public class TaolinkShop extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "淘宝卖家昵称（店铺标识）")
    private String taobaoSellerNick;

    @Schema(description = "淘宝开放平台 session key")
    private String apiSessionKey;

    @Schema(description = "API授权过期时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date apiExpireAt;

    @Schema(description = "状态", example = "active")
    private String status;

    @Schema(description = "绑定平台列表，如 [\"taobao\",\"1688\"]")
    private String bindPlatforms;

    @Schema(description = "绑定人用户ID（JeecgBoot sys_user.id）")
    private String ownerId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否启用监控", example = "true")
    private Boolean monitoringEnabled;

    @Schema(description = "监控回溯天数", example = "7")
    private Integer monitoringDays;
}
