package org.jeecg.modules.taolink.service.impl;

import org.jeecg.modules.taolink.entity.TaolinkSearchCache;
import org.jeecg.modules.taolink.mapper.TaolinkSearchCacheMapper;
import org.jeecg.modules.taolink.service.ITaolinkSearchCacheService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 搜索缓存服务实现类
 * 实现三级缓存链：Redis → DB → Onebound API
 */
@Service
public class TaolinkSearchCacheServiceImpl extends ServiceImpl<TaolinkSearchCacheMapper, TaolinkSearchCache> implements ITaolinkSearchCacheService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private TaolinkSearchCacheMapper taolinkSearchCacheMapper;

    // Redis 缓存前缀
    private static final String REDIS_PREFIX = "taolink:search:cache:";
    // 缓存过期时间（24小时）
    private static final long CACHE_EXPIRE_HOURS = 24;

    // 日志记录器
    private static final Logger log = LoggerFactory.getLogger(TaolinkSearchCacheServiceImpl.class);

    @Override
    public String getCache(String cacheKey) {
        // 1. 从 Redis 获取
        String redisKey = REDIS_PREFIX + cacheKey;
        String result = redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.hasText(result)) {
            // 增加命中次数
            incrementHitCount(cacheKey);
            return result;
        }

        // 2. 从数据库获取
            TaolinkSearchCache cache = baseMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaolinkSearchCache>()
                            .eq(TaolinkSearchCache::getCacheKey, cacheKey)
                            .gt(TaolinkSearchCache::getExpiresAt, new Date())
            );

        if (cache != null) {
            // 写回 Redis
            redisTemplate.opsForValue().set(redisKey, cache.getResultJson(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            // 增加命中次数
            incrementHitCount(cacheKey);
            return cache.getResultJson();
        }

        return null;
    }

    @Override
    public boolean setCache(String cacheKey, String platform, String searchParams, String resultJson) {
        try {
            // 1. 写入 Redis
            String redisKey = REDIS_PREFIX + cacheKey;
            redisTemplate.opsForValue().set(redisKey, resultJson, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

            // 2. 写入数据库
            Date now = new Date();
            Date expiresAt = new Date(now.getTime() + CACHE_EXPIRE_HOURS * 60 * 60 * 1000);

            TaolinkSearchCache cache = baseMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaolinkSearchCache>()
                            .eq(TaolinkSearchCache::getCacheKey, cacheKey)
            );

            if (cache == null) {
                // 新增
                cache = new TaolinkSearchCache()
                        .setCacheKey(cacheKey)
                        .setPlatform(platform)
                        .setSearchParams(searchParams)
                        .setResultJson(resultJson)
                        .setHitCount(0)
                        .setExpiresAt(expiresAt)
                        .setCreateTime(now)
                        .setUpdateTime(now);
                baseMapper.insert(cache);
            } else {
                // 更新
                cache.setPlatform(platform)
                        .setSearchParams(searchParams)
                        .setResultJson(resultJson)
                        .setExpiresAt(expiresAt)
                        .setUpdateTime(now);
                baseMapper.updateById(cache);
            }

            return true;
        } catch (Exception e) {
            log.error("设置搜索缓存失败", e);
            return false;
        }
    }

    @Override
    public int incrementHitCount(String cacheKey) {
        try {
            // 从数据库获取缓存记录
            TaolinkSearchCache cache = baseMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaolinkSearchCache>()
                            .eq(TaolinkSearchCache::getCacheKey, cacheKey)
            );

            if (cache != null) {
                int newHitCount = (cache.getHitCount() == null ? 0 : cache.getHitCount()) + 1;
                cache.setHitCount(newHitCount)
                        .setUpdateTime(new Date());
                baseMapper.updateById(cache);
                return newHitCount;
            }
        } catch (Exception e) {
            log.error("增加命中次数失败", e);
        }
        return 0;
    }

    @Override
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // 总缓存数
            long totalCache = baseMapper.selectCount(null);
            stats.put("totalCache", totalCache);

            // 今日新增缓存数
            Date today = new Date();
            today.setHours(0);
            today.setMinutes(0);
            today.setSeconds(0);
            long todayCache = baseMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaolinkSearchCache>()
                            .ge(TaolinkSearchCache::getCreateTime, today)
            );
            stats.put("todayCache", todayCache);

            // 总命中次数
            Integer totalHits = baseMapper.selectObjs(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaolinkSearchCache>()
                            .select(TaolinkSearchCache::getHitCount)
            ).stream()
                    .map(obj -> (Integer) obj)
                    .filter(hit -> hit != null)
                    .reduce(0, Integer::sum);
            stats.put("totalHits", totalHits);

            // 过期缓存数
            long expiredCache = baseMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaolinkSearchCache>()
                            .lt(TaolinkSearchCache::getExpiresAt, new Date())
            );
            stats.put("expiredCache", expiredCache);

        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
        }

        return stats;
    }

    @Override
    public int clearExpiredCache() {
        try {
            // 删除数据库中过期的缓存
            int deleted = baseMapper.delete(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaolinkSearchCache>()
                            .lt(TaolinkSearchCache::getExpiresAt, new Date())
            );
            return deleted;
        } catch (Exception e) {
            log.error("清除过期缓存失败", e);
            return 0;
        }
    }
}
