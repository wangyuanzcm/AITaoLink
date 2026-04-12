package org.jeecg.modules.taolink.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.taolink.service.ITaolinkSearchCacheService;
import org.jeecg.modules.taolink.integrations.onebound.OneboundClient;
import org.jeecg.modules.taolink.entity.TaolinkSourceOffer;
import org.jeecg.modules.taolink.service.ITaolinkSourceOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 搜索中心控制器
 * 实现统一搜索接口，支持1688平台，集成三级缓存链：Redis → DB → Onebound API
 */
@Tag(name = "搜索中心")
@RestController
@RequestMapping("/taolink/unified-search")
public class TaolinkSearchController {
    @Autowired
    private ITaolinkSearchCacheService taolinkSearchCacheService;

    @Autowired
    private OneboundClient oneboundClient;

    @Autowired
    private ITaolinkSourceOfferService taolinkSourceOfferService;

    /**
     * 统一搜索接口
     * @param platform 平台（1688/taobao）
     * @param keyword 搜索关键词
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 搜索结果
     */
    @Operation(summary="统一搜索接口", description="支持1688平台，集成三级缓存")
    @GetMapping(value = "/unified")
    public Result<Map<String, Object>> unifiedSearch(
            @Parameter(name="platform", description="平台：1688/taobao", required=true) @RequestParam String platform,
            @Parameter(name="keyword", description="搜索关键词", required=true) @RequestParam String keyword,
            @Parameter(name="pageNo", description="页码") @RequestParam(defaultValue="1") Integer pageNo,
            @Parameter(name="pageSize", description="每页数量") @RequestParam(defaultValue="10") Integer pageSize
    ) {
        try {
            // 生成缓存键
            String cacheKey = generateCacheKey(platform, keyword, pageNo, pageSize);

            // 1. 尝试从缓存获取
            String cachedResult = taolinkSearchCacheService.getCache(cacheKey);
            if (cachedResult != null) {
                // 缓存命中
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("data", com.alibaba.fastjson.JSON.parse(cachedResult));
                result.put("cacheHit", true);
                return Result.OK(result);
            }

            // 2. 缓存未命中，调用外部API
            Map<String, Object> apiResult = null;
            if ("1688".equals(platform)) {
                apiResult = oneboundClient.search1688Products(keyword, pageNo, pageSize);
            } else if ("taobao".equals(platform)) {
                // 后续支持淘宝
                return Result.error("淘宝平台暂未支持");
            } else {
                return Result.error("不支持的平台");
            }

            // 3. 缓存结果
            if (apiResult != null) {
                String resultJson = com.alibaba.fastjson.JSON.toJSONString(apiResult);
                taolinkSearchCacheService.setCache(cacheKey, platform, 
                        com.alibaba.fastjson.JSON.toJSONString(Map.of("keyword", keyword, "pageNo", pageNo, "pageSize", pageSize)),
                        resultJson);
            }

            // 4. 返回结果
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("data", apiResult);
            result.put("cacheHit", false);
            return Result.OK(result);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 缓存命中统计接口
     * @return 统计数据
     */
    @Operation(summary="缓存命中统计", description="返回今日命中/未命中/总量等统计数据")
    @GetMapping(value = "/stats")
    public Result<Map<String, Object>> getCacheStats() {
        try {
            Map<String, Object> stats = taolinkSearchCacheService.getCacheStats();
            return Result.OK(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计数据失败");
        }
    }

    /**
     * 清除过期缓存
     * @return 清除结果
     */
    @Operation(summary="清除过期缓存", description="清除数据库中过期的缓存记录")
    @DeleteMapping(value = "/clear-expired")
    public Result<String> clearExpiredCache() {
        try {
            int deleted = taolinkSearchCacheService.clearExpiredCache();
            return Result.OK("清除成功，共清除 " + deleted + " 条过期缓存");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("清除失败：" + e.getMessage());
        }
    }

    /**
     * 搜索结果转 source_offer 落库
     * @param searchResult 搜索结果
     * @return 导入结果
     */
    @Operation(summary="搜索结果转 source_offer 落库", description="将搜索结果批量写入 source_offer 表")
    @PostMapping(value = "/import-to-source-offer")
    public Result<String> importToSourceOffer(@Parameter(name="searchResult", description="搜索结果", required=true) @RequestBody Map<String, Object> searchResult) {
        try {
            List<Map<String, Object>> items = (List<Map<String, Object>>) searchResult.get("items");
            if (items == null || items.isEmpty()) {
                return Result.error("搜索结果为空");
            }

            List<TaolinkSourceOffer> sourceOffers = new ArrayList<>();
            Date now = new Date();

            for (Map<String, Object> item : items) {
                // 提取商品信息
                String numIid = item.get("num_iid").toString();
                String title = item.get("title").toString();
                String detailUrl = item.get("detail_url").toString();
                String sellerNick = item.get("seller_nick").toString();
                String price = item.get("price").toString();
                String platform = item.get("platform") != null ? item.get("platform").toString() : "1688";

                // 转换价格为分
                Integer priceMin = 0;
                Integer priceMax = 0;
                try {
                    double priceDouble = Double.parseDouble(price);
                    priceMin = (int) (priceDouble * 100);
                    priceMax = priceMin;
                } catch (Exception e) {
                    // 价格解析失败，使用默认值
                }

                // 检查是否已存在
                TaolinkSourceOffer existing = taolinkSourceOfferService.getOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TaolinkSourceOffer>()
                                .eq(TaolinkSourceOffer::getNumIid, numIid)
                                .eq(TaolinkSourceOffer::getPlatform, platform)
                );

                if (existing == null) {
                    // 创建新记录
                    TaolinkSourceOffer sourceOffer = new TaolinkSourceOffer()
                            .setPlatform(platform)
                            .setNumIid(numIid)
                            .setTitle(title)
                            .setDetailUrl(detailUrl)
                            .setSellerNick(sellerNick)
                            .setPriceMin(priceMin)
                            .setPriceMax(priceMax)
                            .setRawJson(com.alibaba.fastjson.JSON.toJSONString(item))
                            .setFetchedAt(now);
                    sourceOffers.add(sourceOffer);
                }
            }

            // 批量保存
            if (!sourceOffers.isEmpty()) {
                taolinkSourceOfferService.saveBatch(sourceOffers);
                return Result.OK("导入成功，共导入 " + sourceOffers.size() + " 条记录");
            } else {
                return Result.OK("没有新记录需要导入");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 生成缓存键
     * @param platform 平台
     * @param keyword 关键词
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 缓存键
     */
    private String generateCacheKey(String platform, String keyword, Integer pageNo, Integer pageSize) {
        return platform + ":" + keyword + ":" + pageNo + ":" + pageSize;
    }
}
