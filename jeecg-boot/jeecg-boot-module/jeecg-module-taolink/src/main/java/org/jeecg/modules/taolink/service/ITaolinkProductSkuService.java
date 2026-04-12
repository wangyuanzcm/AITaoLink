package org.jeecg.modules.taolink.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.taolink.entity.TaolinkProductSku;

import java.util.List;

public interface ITaolinkProductSkuService extends JeecgService<TaolinkProductSku> {
    /**
     * 根据商品ID查询SKU列表
     * @param productId 商品ID
     * @return SKU列表
     */
    List<TaolinkProductSku> listByProductId(String productId);
}

