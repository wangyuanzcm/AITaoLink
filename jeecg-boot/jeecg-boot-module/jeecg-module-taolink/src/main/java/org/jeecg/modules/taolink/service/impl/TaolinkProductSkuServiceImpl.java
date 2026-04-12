package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.taolink.entity.TaolinkProductSku;
import org.jeecg.modules.taolink.mapper.TaolinkProductSkuMapper;
import org.jeecg.modules.taolink.service.ITaolinkProductSkuService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaolinkProductSkuServiceImpl extends ServiceImpl<TaolinkProductSkuMapper, TaolinkProductSku> implements ITaolinkProductSkuService {

    @Override
    public List<TaolinkProductSku> listByProductId(String productId) {
        QueryWrapper<TaolinkProductSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        return this.list(queryWrapper);
    }
}

