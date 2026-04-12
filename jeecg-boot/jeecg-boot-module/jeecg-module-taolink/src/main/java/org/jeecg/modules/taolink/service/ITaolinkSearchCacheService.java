package org.jeecg.modules.taolink.service;

import org.jeecg.modules.taolink.entity.TaolinkSearchCache;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;

/**
 * 搜索缓存服务接口
 * 实现三级缓存链：Redis → DB → Onebound API
 */
public interface ITaolinkSearchCacheService extends IService<TaolinkSearchCache> {

    /**
     * 从缓存获取搜索结果
     * @param cacheKey 缓存键
     * @return 搜索结果（JSON字符串）
     */
    String getCache(String cacheKey);

    /**
     * 设置搜索缓存
     * @param cacheKey 缓存键
     * @param platform 平台
     * @param searchParams 搜索参数（JSON格式）
     * @param resultJson 搜索结果（JSON格式）
     * @return 是否设置成功
     */
    boolean setCache(String cacheKey, String platform, String searchParams, String resultJson);

    /**
     * 增加缓存命中次数
     * @param cacheKey 缓存键
     * @return 增加后的命中次数
     */
    int incrementHitCount(String cacheKey);

    /**
     * 获取缓存命中统计
     * @return 统计数据
     */
    Map<String, Object> getCacheStats();

    /**
     * 清除过期缓存
     * @return 清除的缓存数量
     */
    int clearExpiredCache();
}
