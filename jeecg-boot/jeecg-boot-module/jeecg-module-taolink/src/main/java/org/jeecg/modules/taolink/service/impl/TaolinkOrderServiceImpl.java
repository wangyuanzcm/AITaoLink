package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.taolink.entity.TaolinkOrder;
import org.jeecg.modules.taolink.mapper.TaolinkOrderMapper;
import org.jeecg.modules.taolink.service.ITaolinkOrderService;
import org.springframework.stereotype.Service;

@Service
public class TaolinkOrderServiceImpl extends ServiceImpl<TaolinkOrderMapper, TaolinkOrder> implements ITaolinkOrderService {
}

