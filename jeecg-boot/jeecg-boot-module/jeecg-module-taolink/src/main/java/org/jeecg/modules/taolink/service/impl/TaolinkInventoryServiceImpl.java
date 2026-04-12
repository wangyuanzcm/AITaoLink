package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.taolink.entity.TaolinkInventory;
import org.jeecg.modules.taolink.entity.TaolinkInventoryMovement;
import org.jeecg.modules.taolink.mapper.TaolinkInventoryMapper;
import org.jeecg.modules.taolink.mapper.TaolinkInventoryMovementMapper;
import org.jeecg.modules.taolink.service.ITaolinkInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaolinkInventoryServiceImpl extends ServiceImpl<TaolinkInventoryMapper, TaolinkInventory> implements ITaolinkInventoryService {
    @Autowired
    private TaolinkInventoryMovementMapper taolinkInventoryMovementMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> reserve(String warehouseId, String productSkuId, int qty, String refType, String refId) {
        if (qty <= 0) {
            return Result.error("数量必须大于0");
        }
        TaolinkInventory inv = getOrCreate(warehouseId, productSkuId);
        int available = inv.getAvailable() == null ? 0 : inv.getAvailable();
        if (available < qty) {
            return Result.error("库存不足");
        }
        inv.setReserved((inv.getReserved() == null ? 0 : inv.getReserved()) + qty);
        this.updateById(inv);

        TaolinkInventoryMovement mv = new TaolinkInventoryMovement()
                .setWarehouseId(warehouseId)
                .setProductSkuId(productSkuId)
                .setType("reserve")
                .setQty(qty)
                .setRefType(refType)
                .setRefId(refId);
        taolinkInventoryMovementMapper.insert(mv);
        return Result.OK("预占成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> release(String warehouseId, String productSkuId, int qty, String refType, String refId) {
        if (qty <= 0) {
            return Result.error("数量必须大于0");
        }
        TaolinkInventory inv = getOrCreate(warehouseId, productSkuId);
        int reserved = inv.getReserved() == null ? 0 : inv.getReserved();
        if (reserved < qty) {
            return Result.error("预占不足");
        }
        inv.setReserved(reserved - qty);
        this.updateById(inv);

        TaolinkInventoryMovement mv = new TaolinkInventoryMovement()
                .setWarehouseId(warehouseId)
                .setProductSkuId(productSkuId)
                .setType("release")
                .setQty(-qty)
                .setRefType(refType)
                .setRefId(refId);
        taolinkInventoryMovementMapper.insert(mv);
        return Result.OK("释放成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> adjustOnHand(String warehouseId, String productSkuId, int qty, String refType, String refId) {
        if (qty == 0) {
            return Result.error("数量不能为0");
        }
        TaolinkInventory inv = getOrCreate(warehouseId, productSkuId);
        int nextOnHand = (inv.getOnHand() == null ? 0 : inv.getOnHand()) + qty;
        if (nextOnHand < 0) {
            return Result.error("调整后库存不能为负");
        }
        inv.setOnHand(nextOnHand);
        this.updateById(inv);

        TaolinkInventoryMovement mv = new TaolinkInventoryMovement()
                .setWarehouseId(warehouseId)
                .setProductSkuId(productSkuId)
                .setType("adjust")
                .setQty(qty)
                .setRefType(refType)
                .setRefId(refId);
        taolinkInventoryMovementMapper.insert(mv);
        return Result.OK("调整成功");
    }

    private TaolinkInventory getOrCreate(String warehouseId, String productSkuId) {
        LambdaQueryWrapper<TaolinkInventory> qw = new LambdaQueryWrapper<>();
        qw.eq(TaolinkInventory::getWarehouseId, warehouseId);
        qw.eq(TaolinkInventory::getProductSkuId, productSkuId);
        TaolinkInventory inv = this.getOne(qw, false);
        if (inv != null) {
            return inv;
        }
        TaolinkInventory created = new TaolinkInventory()
                .setWarehouseId(warehouseId)
                .setProductSkuId(productSkuId)
                .setOnHand(0)
                .setReserved(0)
                .setWarningMin(5)
                .setOverstockDays(30);
        this.save(created);
        return created;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deduct(String warehouseId, String productSkuId, int qty, String refType, String refId) {
        if (qty <= 0) {
            return Result.error("数量必须大于0");
        }
        TaolinkInventory inv = getOrCreate(warehouseId, productSkuId);
        int reserved = inv.getReserved() == null ? 0 : inv.getReserved();
        if (reserved < qty) {
            return Result.error("预占不足");
        }
        int nextOnHand = (inv.getOnHand() == null ? 0 : inv.getOnHand()) - qty;
        if (nextOnHand < 0) {
            return Result.error("库存不足");
        }
        inv.setOnHand(nextOnHand);
        inv.setReserved(reserved - qty);
        this.updateById(inv);

        TaolinkInventoryMovement mv = new TaolinkInventoryMovement()
                .setWarehouseId(warehouseId)
                .setProductSkuId(productSkuId)
                .setType("deduct")
                .setQty(-qty)
                .setRefType(refType)
                .setRefId(refId);
        taolinkInventoryMovementMapper.insert(mv);
        return Result.OK("扣减成功");
    }

    @Override
    public List<TaolinkInventory> listByProductSkuId(String productSkuId) {
        LambdaQueryWrapper<TaolinkInventory> qw = new LambdaQueryWrapper<>();
        qw.eq(TaolinkInventory::getProductSkuId, productSkuId);
        return this.list(qw);
    }

    @Override
    public Result<java.util.Map<String, Object>> getInventoryAnalysisOverview() {
        try {
            java.util.Map<String, Object> overview = new java.util.HashMap<>();
            
            // 获取所有库存记录
            List<TaolinkInventory> inventories = this.list();
            
            // 计算总SKU数
            int totalSkus = inventories.size();
            
            // 计算低库存SKU数（可用库存低于预警阈值）
            int lowStockSkus = 0;
            // 计算积压库存SKU数（假设积压天数超过阈值）
            int overstockSkus = 0;
            
            for (TaolinkInventory inv : inventories) {
                int available = inv.getAvailable() == null ? 0 : inv.getAvailable();
                int warningMin = inv.getWarningMin() == null ? 5 : inv.getWarningMin();
                if (available < warningMin) {
                    lowStockSkus++;
                }
                // 这里可以根据实际情况计算积压库存
            }
            
            // 计算总库存价值（假设每个SKU价值100元，实际应该从产品表获取）
            int totalValue = totalSkus * 100;
            
            overview.put("totalSkus", totalSkus);
            overview.put("lowStockSkus", lowStockSkus);
            overview.put("overstockSkus", overstockSkus);
            overview.put("totalValue", totalValue);
            
            return Result.OK(overview);
        } catch (Exception e) {
            return Result.error("获取库存分析概览失败: " + e.getMessage());
        }
    }

    @Override
    public Result<java.util.Map<String, Object>> getInventoryAnalysisMetrics() {
        try {
            java.util.Map<String, Object> metrics = new java.util.HashMap<>();
            
            // 计算库存周转率（假设值）
            double turnoverRate = 3.5;
            // 计算呆滞品比例（假设值）
            double stagnantRatio = 0.15;
            // 计算库存健康度（假设值）
            double healthScore = 85.5;
            
            metrics.put("turnoverRate", turnoverRate);
            metrics.put("stagnantRatio", stagnantRatio);
            metrics.put("healthScore", healthScore);
            
            return Result.OK(metrics);
        } catch (Exception e) {
            return Result.error("获取库存分析指标失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> updateThreshold(String id, Integer warningMin, Integer overstockDays) {
        try {
            TaolinkInventory inventory = this.getById(id);
            if (inventory == null) {
                return Result.error("未找到对应库存记录");
            }
            
            if (warningMin != null) {
                inventory.setWarningMin(warningMin);
            }
            if (overstockDays != null) {
                inventory.setOverstockDays(overstockDays);
            }
            
            this.updateById(inventory);
            return Result.OK("预警阈值更新成功！");
        } catch (Exception e) {
            return Result.error("更新预警阈值失败: " + e.getMessage());
        }
    }
}

