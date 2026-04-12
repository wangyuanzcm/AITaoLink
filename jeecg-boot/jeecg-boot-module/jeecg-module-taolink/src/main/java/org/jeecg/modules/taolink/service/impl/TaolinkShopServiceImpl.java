package org.jeecg.modules.taolink.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.taolink.entity.TaolinkShop;
import org.jeecg.modules.taolink.mapper.TaolinkShopMapper;
import org.jeecg.modules.taolink.service.ITaolinkShopService;
import org.springframework.stereotype.Service;

@Service
public class TaolinkShopServiceImpl extends JeecgServiceImpl<TaolinkShopMapper, TaolinkShop> implements ITaolinkShopService {

    @Override
    public java.util.List<java.util.Map<String, Object>> getShopRanking() {
        // 获取所有店铺
        java.util.List<TaolinkShop> shops = this.list();
        java.util.List<java.util.Map<String, Object>> rankingList = new java.util.ArrayList<>();
        
        for (TaolinkShop shop : shops) {
            java.util.Map<String, Object> shopData = new java.util.HashMap<>();
            shopData.put("shopId", shop.getId());
            shopData.put("shopName", shop.getTaobaoSellerNick());
            
            // 模拟数据：实际应该从订单表统计
            // 这里使用随机数生成模拟数据
            java.util.Random random = new java.util.Random();
            int orderCount = random.nextInt(1000) + 100; // 100-1100订单
            int gmv = random.nextInt(1000000) + 100000; // 10万-110万GMV
            
            shopData.put("orderCount", orderCount);
            shopData.put("gmv", gmv);
            shopData.put("status", shop.getStatus());
            
            rankingList.add(shopData);
        }
        
        // 按GMV降序排序
        rankingList.sort((a, b) -> {
            Integer gmvA = (Integer) a.get("gmv");
            Integer gmvB = (Integer) b.get("gmv");
            return gmvB.compareTo(gmvA);
        });
        
        return rankingList;
    }
}
