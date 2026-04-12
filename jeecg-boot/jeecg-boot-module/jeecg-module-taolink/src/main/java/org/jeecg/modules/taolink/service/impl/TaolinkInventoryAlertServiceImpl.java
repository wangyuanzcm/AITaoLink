package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.taolink.entity.TaolinkInventoryAlert;
import org.jeecg.modules.taolink.mapper.TaolinkInventoryAlertMapper;
import org.jeecg.modules.taolink.service.ITaolinkInventoryAlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class TaolinkInventoryAlertServiceImpl extends ServiceImpl<TaolinkInventoryAlertMapper, TaolinkInventoryAlert> implements ITaolinkInventoryAlertService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createAlert(TaolinkInventoryAlert alert) {
        try {
            // 检查是否已存在相同的未解决预警
            LambdaQueryWrapper<TaolinkInventoryAlert> qw = new LambdaQueryWrapper<>();
            qw.eq(TaolinkInventoryAlert::getProductSkuId, alert.getProductSkuId());
            qw.eq(TaolinkInventoryAlert::getAlertType, alert.getAlertType());
            qw.eq(TaolinkInventoryAlert::getStatus, "pending");
            if (this.count(qw) > 0) {
                return Result.error("已存在相同的未解决预警");
            }

            // 设置默认状态
            alert.setStatus("pending");
            this.save(alert);
            return Result.OK("预警创建成功");
        } catch (Exception e) {
            return Result.error("创建预警失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> resolveAlert(String id, String handler) {
        try {
            TaolinkInventoryAlert alert = this.getById(id);
            if (alert == null) {
                return Result.error("未找到对应预警记录");
            }

            alert.setStatus("resolved");
            alert.setHandler(handler);
            alert.setHandledAt(new java.util.Date());
            this.updateById(alert);
            return Result.OK("预警已解决");
        } catch (Exception e) {
            return Result.error("解决预警失败: " + e.getMessage());
        }
    }

    @Override
    public List<TaolinkInventoryAlert> listAlerts(String alertType, String status) {
        LambdaQueryWrapper<TaolinkInventoryAlert> qw = new LambdaQueryWrapper<>();
        if (alertType != null && !alertType.isEmpty()) {
            qw.eq(TaolinkInventoryAlert::getAlertType, alertType);
        }
        if (status != null && !status.isEmpty()) {
            qw.eq(TaolinkInventoryAlert::getStatus, status);
        }
        qw.orderByDesc(TaolinkInventoryAlert::getCreateTime);
        return this.list(qw);
    }

    @Override
    public Result<Map<String, Object>> getAlertStats() {
        try {
            Map<String, Object> stats = new java.util.HashMap<>();

            // 计算总预警数
            long totalAlerts = this.count();

            // 计算未解决预警数
            long pendingAlerts = this.count(new LambdaQueryWrapper<TaolinkInventoryAlert>()
                    .eq(TaolinkInventoryAlert::getStatus, "pending"));

            // 计算已解决预警数
            long resolvedAlerts = this.count(new LambdaQueryWrapper<TaolinkInventoryAlert>()
                    .eq(TaolinkInventoryAlert::getStatus, "resolved"));

            // 计算低库存预警数
            long lowStockAlerts = this.count(new LambdaQueryWrapper<TaolinkInventoryAlert>()
                    .eq(TaolinkInventoryAlert::getAlertType, "low_stock"));

            // 计算积压库存预警数
            long overstockAlerts = this.count(new LambdaQueryWrapper<TaolinkInventoryAlert>()
                    .eq(TaolinkInventoryAlert::getAlertType, "overstock"));

            stats.put("totalAlerts", totalAlerts);
            stats.put("pendingAlerts", pendingAlerts);
            stats.put("resolvedAlerts", resolvedAlerts);
            stats.put("lowStockAlerts", lowStockAlerts);
            stats.put("overstockAlerts", overstockAlerts);

            return Result.OK(stats);
        } catch (Exception e) {
            return Result.error("获取预警统计失败: " + e.getMessage());
        }
    }
}
