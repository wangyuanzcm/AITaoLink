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
@Data
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
}
