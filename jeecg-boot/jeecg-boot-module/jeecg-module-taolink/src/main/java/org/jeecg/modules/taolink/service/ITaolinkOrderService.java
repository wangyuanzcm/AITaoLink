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

    /**
     * 统计当日订单数
     */
    int countTodayOrders();

    /**
     * 统计当日GMV
     */
    int calculateTodayGmv();

    /**
     * 统计订单异常预警
     */
    long countOrderAlerts();

    /**
     * 统计退款异常预警
     */
    long countRefundAlerts();

    /**
     * 获取全局趋势数据
     * @param days 统计天数
     */
    java.util.List<java.util.Map<String, Object>> getGlobalTrend(Integer days);
}

