package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.taolink.entity.TaolinkOrderLine;
import org.jeecg.modules.taolink.mapper.TaolinkOrderLineMapper;
import org.jeecg.modules.taolink.service.ITaolinkOrderLineService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaolinkOrderLineServiceImpl extends ServiceImpl<TaolinkOrderLineMapper, TaolinkOrderLine> implements ITaolinkOrderLineService {

    @Override
    public List<TaolinkOrderLine> listByOrderId(String orderId) {
        QueryWrapper<TaolinkOrderLine> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        return this.list(queryWrapper);
    }
}

