package org.jeecg.modules.taolink.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.util.Date;

/**
 * 搜索缓存记录实体类
 * 用于存储搜索结果缓存，支持三级缓存链：Redis → DB → Onebound API
 */
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("taolink_search_cache")
public class TaolinkSearchCache implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 缓存键（唯一标识）
     */
    private String cacheKey;

    /**
     * 平台（1688/taobao）
     */
    private String platform;

    /**
     * 搜索参数（JSON格式）
     */
    private String searchParams;

    /**
     * 搜索结果（JSON格式）
     */
    private String resultJson;

    /**
     * 命中次数
     */
    private Integer hitCount;

    /**
     * 过期时间
     */
    private Date expiresAt;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 租户ID
     */
    private String tenantId;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public TaolinkSearchCache setId(String id) {
        this.id = id;
        return this;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public TaolinkSearchCache setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public TaolinkSearchCache setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public String getSearchParams() {
        return searchParams;
    }

    public TaolinkSearchCache setSearchParams(String searchParams) {
        this.searchParams = searchParams;
        return this;
    }

    public String getResultJson() {
        return resultJson;
    }

    public TaolinkSearchCache setResultJson(String resultJson) {
        this.resultJson = resultJson;
        return this;
    }

    public Integer getHitCount() {
        return hitCount;
    }

    public TaolinkSearchCache setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
        return this;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public TaolinkSearchCache setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public TaolinkSearchCache setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public TaolinkSearchCache setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public String getTenantId() {
        return tenantId;
    }

    public TaolinkSearchCache setTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }
}
