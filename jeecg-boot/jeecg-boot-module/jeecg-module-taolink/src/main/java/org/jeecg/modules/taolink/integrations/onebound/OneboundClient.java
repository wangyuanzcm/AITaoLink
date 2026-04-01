package org.jeecg.modules.taolink.integrations.onebound;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.RestUtil;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OneboundClient {
    @Value("${taolink.onebound.base-url:https://open.onebound.cn}")
    private String baseUrl;

    @Value("${taolink.onebound.api-key:}")
    private String apiKey;

    public Result<JSONObject> fetchTaobaoItem(String numIid) {
        return get("/taobao/item_get", numIid);
    }

    public Result<JSONObject> fetch1688Item(String numIid) {
        return get("/1688/item_get", numIid);
    }

    private Result<JSONObject> get(String path, String numIid) {
        if (oConvertUtils.isEmpty(apiKey)) {
            return Result.error("未配置 taolink.onebound.api-key");
        }
        if (oConvertUtils.isEmpty(numIid)) {
            return Result.error("numIid不能为空");
        }
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(path)
                .queryParam("key", apiKey)
                .queryParam("num_iid", numIid)
                .build(true)
                .toUriString();
        JSONObject json = RestUtil.get(url);
        return Result.OK(json);
    }
}

