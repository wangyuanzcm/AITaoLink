package org.jeecg.modules.taolink.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.taolink.entity.TaolinkInventory;
import org.jeecg.modules.taolink.entity.TaolinkInventoryAlert;
import org.jeecg.modules.taolink.service.ITaolinkInventoryService;
import org.jeecg.modules.taolink.service.ITaolinkInventoryAlertService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class InventoryAlertCheckJob implements Job {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InventoryAlertCheckJob.class);

    @Autowired
    private ITaolinkInventoryService taolinkInventoryService;

    @Autowired
    private ITaolinkInventoryAlertService taolinkInventoryAlertService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始执行库存预警检查任务");

        try {
            // 获取所有库存记录
            List<TaolinkInventory> inventories = taolinkInventoryService.list();

            for (TaolinkInventory inventory : inventories) {
                // 检查低库存
                checkLowStock(inventory);

                // 检查积压库存（这里简单实现，实际应该根据库存流转时间计算）
                checkOverstock(inventory);
            }

            log.info("库存预警检查任务执行完成");
        } catch (Exception e) {
            log.error("库存预警检查任务执行失败", e);
            throw new JobExecutionException(e);
        }
    }

    /**
     * 检查低库存
     * @param inventory 库存记录
     */
    private void checkLowStock(TaolinkInventory inventory) {
        int available = inventory.getAvailable() == null ? 0 : inventory.getAvailable();
        int warningMin = inventory.getWarningMin() == null ? 5 : inventory.getWarningMin();

        if (available < warningMin) {
            // 创建低库存预警
            TaolinkInventoryAlert alert = new TaolinkInventoryAlert()
                    .setProductSkuId(inventory.getProductSkuId())
                    .setAlertType("low_stock")
                    .setThreshold(warningMin)
                    .setCurrentValue(available)
                    .setMessage("商品SKU " + inventory.getProductSkuId() + " 库存不足，当前可用库存: " + available + "，预警阈值: " + warningMin);

            taolinkInventoryAlertService.createAlert(alert);
            log.info("创建低库存预警: {}", alert.getMessage());
        }
    }

    /**
     * 检查积压库存
     * @param inventory 库存记录
     */
    private void checkOverstock(TaolinkInventory inventory) {
        // 这里简单实现，实际应该根据库存流转时间计算积压天数
        // 假设库存数量超过 100 就算积压
        int onHand = inventory.getOnHand() == null ? 0 : inventory.getOnHand();
        int overstockDays = inventory.getOverstockDays() == null ? 30 : inventory.getOverstockDays();

        if (onHand > 100) {
            // 创建积压库存预警
            TaolinkInventoryAlert alert = new TaolinkInventoryAlert()
                    .setProductSkuId(inventory.getProductSkuId())
                    .setAlertType("overstock")
                    .setThreshold(100)
                    .setCurrentValue(onHand)
                    .setMessage("商品SKU " + inventory.getProductSkuId() + " 库存积压，当前库存: " + onHand + "，超过阈值: 100");

            taolinkInventoryAlertService.createAlert(alert);
            log.info("创建积压库存预警: {}", alert.getMessage());
        }
    }
}
