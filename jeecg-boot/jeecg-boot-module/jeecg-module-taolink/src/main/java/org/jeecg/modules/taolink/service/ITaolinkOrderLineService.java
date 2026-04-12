package org.jeecg.modules.taolink.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.taolink.entity.TaolinkOrderLine;

import java.util.List;

public interface ITaolinkOrderLineService extends JeecgService<TaolinkOrderLine> {
    /**
     * 根据订单ID查询订单商品行
     * @param orderId 订单ID
     * @return 订单商品行列表
     */
    List<TaolinkOrderLine> listByOrderId(String orderId);
}

