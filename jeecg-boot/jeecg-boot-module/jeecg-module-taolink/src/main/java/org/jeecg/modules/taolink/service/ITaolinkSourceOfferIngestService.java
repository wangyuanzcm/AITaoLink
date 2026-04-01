package org.jeecg.modules.taolink.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.taolink.entity.TaolinkSourceOffer;

public interface ITaolinkSourceOfferIngestService {
    Result<TaolinkSourceOffer> fetchAndUpsert(String platform, String numIid);
}

