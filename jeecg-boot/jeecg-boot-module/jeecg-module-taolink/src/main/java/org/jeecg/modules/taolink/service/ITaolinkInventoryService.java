package org.jeecg.modules.taolink.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.taolink.entity.TaolinkInventory;

public interface ITaolinkInventoryService extends JeecgService<TaolinkInventory> {
    Result<String> reserve(String warehouseId, String productSkuId, int qty, String refType, String refId);

    Result<String> release(String warehouseId, String productSkuId, int qty, String refType, String refId);

    Result<String> adjustOnHand(String warehouseId, String productSkuId, int qty, String refType, String refId);
}

