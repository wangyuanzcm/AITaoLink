package org.jeecg.modules.taolink.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.taolink.entity.TaolinkSearchCache;
import org.jeecg.modules.taolink.service.ITaolinkSearchCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "TaoLink-搜索缓存")
@RestController
@RequestMapping("/taolink/search")
public class TaolinkSearchCacheController extends JeecgController<TaolinkSearchCache, ITaolinkSearchCacheService> {
    @Autowired
    private ITaolinkSearchCacheService taolinkSearchCacheService;

    @Operation(summary = "分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<TaolinkSearchCache>> list(TaolinkSearchCache cache,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                   HttpServletRequest req) {
        QueryWrapper<TaolinkSearchCache> queryWrapper = QueryGenerator.initQueryWrapper(cache, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<TaolinkSearchCache> page = new Page<>(pageNo, pageSize);
        IPage<TaolinkSearchCache> pageList = taolinkSearchCacheService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "搜索缓存统计")
    @GetMapping(value = "/stats")
    public Result<Map<String, Object>> searchStats() {
        try {
            QueryWrapper<TaolinkSearchCache> query = new QueryWrapper<>();
            java.util.List<TaolinkSearchCache> allCaches = taolinkSearchCacheService.list(query);

            int totalHitCount = allCaches.stream()
                    .mapToInt(c -> c.getHitCount() != null ? c.getHitCount() : 0)
                    .sum();
            int totalCached = allCaches.size();
            int expiredCount = (int) allCaches.stream()
                    .filter(c -> c.getExpiresAt() != null && c.getExpiresAt().before(new java.util.Date()))
                    .count();
            int validCount = totalCached - expiredCount;

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCached", totalCached);
            stats.put("validCount", validCount);
            stats.put("expiredCount", expiredCount);
            stats.put("totalHitCount", totalHitCount);

            return Result.OK(stats);
        } catch (Exception e) {
            log.error("搜索缓存统计失败", e);
            return Result.error("获取缓存统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "清空所有搜索缓存")
    @DeleteMapping(value = "/cache/clear")
    public Result<String> clearCache() {
        try {
            taolinkSearchCacheService.remove(null);
            return Result.OK("缓存已清空！");
        } catch (Exception e) {
            log.error("清空缓存失败", e);
            return Result.error("清空缓存失败: " + e.getMessage());
        }
    }
}
