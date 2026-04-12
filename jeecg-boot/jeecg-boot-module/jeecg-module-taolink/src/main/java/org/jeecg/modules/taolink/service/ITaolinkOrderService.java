package org.jeecg.modules.taolink.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.taolink.entity.TaolinkOrder;

public interface ITaolinkOrderService extends JeecgService<TaolinkOrder> {
    /**
     * 创建订单并预留库存
     * @param order 订单信息
     * @return 操作结果
     */
    Result<String> createOrderWithReserve(TaolinkOrder order);

    /**
     * 取消订单并释放库存
     * @param orderId 订单ID
     * @return 操作结果
     */
    Result<String> cancelOrderWithRelease(String orderId);

    /**
     * 订单发货并扣减库存
     * @param orderId 订单ID
     * @return 操作结果
     */
    Result<String> shipOrderWithDeduct(String orderId);
}

