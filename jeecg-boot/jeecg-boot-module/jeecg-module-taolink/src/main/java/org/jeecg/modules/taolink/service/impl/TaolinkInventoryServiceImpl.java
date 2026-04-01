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
                .setReserved(0);
        this.save(created);
        return created;
    }
}

