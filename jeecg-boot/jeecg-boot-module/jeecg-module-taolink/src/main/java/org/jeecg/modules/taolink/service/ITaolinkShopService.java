package org.jeecg.modules.taolink.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.taolink.entity.TaolinkShop;

public interface ITaolinkShopService extends JeecgService<TaolinkShop> {
    /**
     * 获取店铺排行数据
     */
    java.util.List<java.util.Map<String, Object>> getShopRanking();
}
