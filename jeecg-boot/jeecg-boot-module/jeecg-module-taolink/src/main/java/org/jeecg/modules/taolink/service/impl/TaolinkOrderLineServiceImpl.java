package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.taolink.entity.TaolinkOrderLine;
import org.jeecg.modules.taolink.mapper.TaolinkOrderLineMapper;
import org.jeecg.modules.taolink.service.ITaolinkOrderLineService;
import org.springframework.stereotype.Service;

@Service
public class TaolinkOrderLineServiceImpl extends ServiceImpl<TaolinkOrderLineMapper, TaolinkOrderLine> implements ITaolinkOrderLineService {
}

