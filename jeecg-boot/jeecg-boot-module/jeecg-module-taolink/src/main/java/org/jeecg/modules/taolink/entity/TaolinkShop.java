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

    @Schema(description = "租户ID")
    private String tenantId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否启用监控", example = "true")
    private Boolean monitoringEnabled;

    @Schema(description = "监控回溯天数", example = "7")
    private Integer monitoringDays;

    // Getters and Setters
    public String getTaobaoSellerNick() {
        return taobaoSellerNick;
    }

    public void setTaobaoSellerNick(String taobaoSellerNick) {
        this.taobaoSellerNick = taobaoSellerNick;
    }

    public String getApiSessionKey() {
        return apiSessionKey;
    }

    public void setApiSessionKey(String apiSessionKey) {
        this.apiSessionKey = apiSessionKey;
    }

    public Date getApiExpireAt() {
        return apiExpireAt;
    }

    public void setApiExpireAt(Date apiExpireAt) {
        this.apiExpireAt = apiExpireAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBindPlatforms() {
        return bindPlatforms;
    }

    public void setBindPlatforms(String bindPlatforms) {
        this.bindPlatforms = bindPlatforms;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getMonitoringEnabled() {
        return monitoringEnabled;
    }

    public void setMonitoringEnabled(Boolean monitoringEnabled) {
        this.monitoringEnabled = monitoringEnabled;
    }

    public Integer getMonitoringDays() {
        return monitoringDays;
    }

    public void setMonitoringDays(Integer monitoringDays) {
        this.monitoringDays = monitoringDays;
    }
}
