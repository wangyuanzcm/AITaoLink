package org.jeecg.modules.taolink.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.taolink.entity.TaolinkProduct;

public interface ITaolinkProductService extends JeecgService<TaolinkProduct> {
    /**
     * 统计在售商品数量
     */
    long countListedProducts();
}

