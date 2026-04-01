package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.taolink.entity.TaolinkProduct;
import org.jeecg.modules.taolink.mapper.TaolinkProductMapper;
import org.jeecg.modules.taolink.service.ITaolinkProductService;
import org.springframework.stereotype.Service;

@Service
public class TaolinkProductServiceImpl extends ServiceImpl<TaolinkProductMapper, TaolinkProduct> implements ITaolinkProductService {
}

