package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.taolink.entity.TaolinkInventory;
import org.jeecg.modules.taolink.entity.TaolinkProduct;
import org.jeecg.modules.taolink.entity.TaolinkProductSku;
import org.jeecg.modules.taolink.mapper.TaolinkProductMapper;
import org.jeecg.modules.taolink.service.ITaolinkInventoryService;
import org.jeecg.modules.taolink.service.ITaolinkProductService;
import org.jeecg.modules.taolink.service.ITaolinkProductSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaolinkProductServiceImpl extends ServiceImpl<TaolinkProductMapper, TaolinkProduct> implements ITaolinkProductService {

    @Autowired
    private ITaolinkProductSkuService productSkuService;

    @Autowired
    private ITaolinkInventoryService inventoryService;

    @Override
    public boolean save(TaolinkProduct product) {
        boolean saved = super.save(product);
        // 商品上架时自动创建初始库存记录
        if (saved && "listed".equals(product.getListingStatus())) {
            createInitialInventory(product.getId());
        }
        return saved;
    }

    @Override
    public boolean updateById(TaolinkProduct product) {
        boolean updated = super.updateById(product);
        // 商品从非上架状态变为上架状态时，创建初始库存记录
        if (updated && "listed".equals(product.getListingStatus())) {
            // 检查是否已有库存记录
            List<TaolinkProductSku> skus = productSkuService.listByProductId(product.getId());
            for (TaolinkProductSku sku : skus) {
                List<TaolinkInventory> inventories = inventoryService.listByProductSkuId(sku.getId());
                if (inventories.isEmpty()) {
                    createInventoryForSku(sku.getId());
                }
            }
        }
        return updated;
    }

    /**
     * 为商品的所有SKU创建初始库存记录
     * @param productId 商品ID
     */
    private void createInitialInventory(String productId) {
        List<TaolinkProductSku> skus = productSkuService.listByProductId(productId);
        for (TaolinkProductSku sku : skus) {
            createInventoryForSku(sku.getId());
        }
    }

    /**
     * 为单个SKU创建初始库存记录
     * @param skuId SKU ID
     */
    private void createInventoryForSku(String skuId) {
        TaolinkInventory inventory = new TaolinkInventory();
        inventory.setProductSkuId(skuId);
        // 默认仓库（可以根据实际情况调整）
        inventory.setWarehouseId("default");
        inventory.setOnHand(0);
        inventory.setReserved(0);
        inventory.setWarningMin(5); // 默认低库存预警值
        inventory.setOverstockDays(30); // 默认积压天数阈值
        inventoryService.save(inventory);
    }
}

