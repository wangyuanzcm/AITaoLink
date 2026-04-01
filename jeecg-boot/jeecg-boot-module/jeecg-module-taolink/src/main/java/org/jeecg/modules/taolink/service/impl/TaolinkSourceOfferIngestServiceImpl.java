package org.jeecg.modules.taolink.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.taolink.entity.TaolinkSourceOffer;
import org.jeecg.modules.taolink.integrations.onebound.OneboundClient;
import org.jeecg.modules.taolink.service.ITaolinkSourceOfferIngestService;
import org.jeecg.modules.taolink.service.ITaolinkSourceOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class TaolinkSourceOfferIngestServiceImpl implements ITaolinkSourceOfferIngestService {
    @Autowired
    private OneboundClient oneboundClient;

    @Autowired
    private ITaolinkSourceOfferService taolinkSourceOfferService;

    @Override
    public Result<TaolinkSourceOffer> fetchAndUpsert(String platform, String numIid) {
        if (oConvertUtils.isEmpty(platform)) {
            return Result.error("platform不能为空");
        }
        if (oConvertUtils.isEmpty(numIid)) {
            return Result.error("numIid不能为空");
        }
        String normalizedPlatform = platform.trim().toLowerCase();
        Result<JSONObject> fetchResult;
        if ("taobao".equals(normalizedPlatform) || "tb".equals(normalizedPlatform)) {
            fetchResult = oneboundClient.fetchTaobaoItem(numIid);
            normalizedPlatform = "taobao";
        } else if ("1688".equals(normalizedPlatform) || "alibaba".equals(normalizedPlatform)) {
            fetchResult = oneboundClient.fetch1688Item(numIid);
            normalizedPlatform = "1688";
        } else {
            return Result.error("不支持的平台: " + platform);
        }
        if (!fetchResult.isSuccess() || fetchResult.getResult() == null) {
            return Result.error(oConvertUtils.getString(fetchResult.getMessage(), "采集失败"));
        }
        JSONObject raw = fetchResult.getResult();

        LambdaQueryWrapper<TaolinkSourceOffer> qw = new LambdaQueryWrapper<>();
        qw.eq(TaolinkSourceOffer::getPlatform, normalizedPlatform);
        qw.eq(TaolinkSourceOffer::getNumIid, numIid);
        TaolinkSourceOffer existing = taolinkSourceOfferService.getOne(qw, false);

        TaolinkSourceOffer offer = existing == null ? new TaolinkSourceOffer() : existing;
        offer.setPlatform(normalizedPlatform);
        offer.setNumIid(numIid);
        offer.setTitle(firstNonEmpty(
                getByPath(raw, "item.title"),
                getByPath(raw, "data.item.title"),
                raw.getString("title")
        ));
        offer.setDetailUrl(firstNonEmpty(
                getByPath(raw, "item.detail_url"),
                getByPath(raw, "data.item.detail_url"),
                raw.getString("detail_url")
        ));
        offer.setSellerNick(firstNonEmpty(
                getByPath(raw, "item.nick"),
                getByPath(raw, "data.item.nick"),
                raw.getString("nick"),
                raw.getString("seller_nick")
        ));
        offer.setLocation(firstNonEmpty(
                getByPath(raw, "item.provcity"),
                getByPath(raw, "data.item.provcity"),
                raw.getString("provcity"),
                raw.getString("location")
        ));
        offer.setMinNum(parseIntSafe(firstNonEmpty(
                getByPath(raw, "item.min_num"),
                getByPath(raw, "data.item.min_num"),
                raw.getString("min_num")
        )));

        Integer price = parsePriceCent(firstNonEmpty(
                getByPath(raw, "item.price"),
                getByPath(raw, "data.item.price"),
                raw.getString("price")
        ));
        Integer promoPrice = parsePriceCent(firstNonEmpty(
                getByPath(raw, "item.promotion_price"),
                getByPath(raw, "data.item.promotion_price"),
                raw.getString("promotion_price")
        ));
        Integer min = minNonNull(price, promoPrice);
        Integer max = maxNonNull(price, promoPrice);
        offer.setPriceMin(min);
        offer.setPriceMax(max);

        offer.setRawJson(raw.toJSONString());
        offer.setFetchedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        if (existing == null) {
            taolinkSourceOfferService.save(offer);
        } else {
            taolinkSourceOfferService.updateById(offer);
        }
        return Result.OK(offer);
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (oConvertUtils.isNotEmpty(v)) {
                return v;
            }
        }
        return null;
    }

    private static String getByPath(JSONObject json, String path) {
        if (json == null || oConvertUtils.isEmpty(path)) {
            return null;
        }
        String[] parts = path.split("\\.");
        Object current = json;
        for (String part : parts) {
            if (!(current instanceof JSONObject)) {
                return null;
            }
            current = ((JSONObject) current).get(part);
            if (current == null) {
                return null;
            }
        }
        return current instanceof String ? (String) current : String.valueOf(current);
    }

    private static Integer parseIntSafe(String value) {
        if (oConvertUtils.isEmpty(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer parsePriceCent(String value) {
        if (oConvertUtils.isEmpty(value)) {
            return null;
        }
        try {
            BigDecimal bd = new BigDecimal(value.trim());
            return bd.movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer minNonNull(Integer a, Integer b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return Math.min(a, b);
    }

    private static Integer maxNonNull(Integer a, Integer b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return Math.max(a, b);
    }
}
