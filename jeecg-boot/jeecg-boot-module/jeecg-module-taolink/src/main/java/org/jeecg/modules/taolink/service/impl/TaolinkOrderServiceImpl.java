package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.taolink.entity.TaolinkOrder;
import org.jeecg.modules.taolink.entity.TaolinkOrderLine;
import org.jeecg.modules.taolink.mapper.TaolinkOrderMapper;
import org.jeecg.modules.taolink.service.ITaolinkInventoryService;
import org.jeecg.modules.taolink.service.ITaolinkOrderLineService;
import org.jeecg.modules.taolink.service.ITaolinkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaolinkOrderServiceImpl extends ServiceImpl<TaolinkOrderMapper, TaolinkOrder> implements ITaolinkOrderService {

    @Autowired
    private ITaolinkOrderLineService orderLineService;

    @Autowired
    private ITaolinkInventoryService inventoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createOrderWithReserve(TaolinkOrder order) {
        // 保存订单
        if (!this.save(order)) {
            return Result.error("订单创建失败");
        }

        // 获取订单商品行
        List<TaolinkOrderLine> orderLines = orderLineService.listByOrderId(order.getId());
        if (orderLines.isEmpty()) {
            return Result.error("订单无商品信息");
        }

        // 为每个商品预留库存
        for (TaolinkOrderLine line : orderLines) {
            Result<String> reserveResult = inventoryService.reserve(
                    "default", // 默认仓库
                    line.getProductSkuId(),
                    line.getQty(),
                    "order",
                    order.getId()
            );
            if (!reserveResult.isSuccess()) {
                throw new RuntimeException("库存预留失败: " + reserveResult.getMessage());
            }
        }

        return Result.OK("订单创建成功并预留库存");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelOrderWithRelease(String orderId) {
        // 查询订单
        TaolinkOrder order = this.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 获取订单商品行
        List<TaolinkOrderLine> orderLines = orderLineService.listByOrderId(orderId);
        if (orderLines.isEmpty()) {
            return Result.error("订单无商品信息");
        }

        // 释放每个商品的预留库存
        for (TaolinkOrderLine line : orderLines) {
            Result<String> releaseResult = inventoryService.release(
                    "default", // 默认仓库
                    line.getProductSkuId(),
                    line.getQty(),
                    "order",
                    orderId
            );
            if (!releaseResult.isSuccess()) {
                throw new RuntimeException("库存释放失败: " + releaseResult.getMessage());
            }
        }

        // 更新订单状态
        order.setStatus("cancelled");
        this.updateById(order);

        return Result.OK("订单取消成功并释放库存");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> shipOrderWithDeduct(String orderId) {
        // 查询订单
        TaolinkOrder order = this.getById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }

        // 获取订单商品行
        List<TaolinkOrderLine> orderLines = orderLineService.listByOrderId(orderId);
        if (orderLines.isEmpty()) {
            return Result.error("订单无商品信息");
        }

        // 扣减每个商品的库存
        for (TaolinkOrderLine line : orderLines) {
            Result<String> deductResult = inventoryService.deduct(
                    "default", // 默认仓库
                    line.getProductSkuId(),
                    line.getQty(),
                    "order",
                    orderId
            );
            if (!deductResult.isSuccess()) {
                throw new RuntimeException("库存扣减失败: " + deductResult.getMessage());
            }
        }

        // 更新订单状态
        order.setStatus("shipped");
        this.updateById(order);

        return Result.OK("订单发货成功并扣减库存");
    }
}

