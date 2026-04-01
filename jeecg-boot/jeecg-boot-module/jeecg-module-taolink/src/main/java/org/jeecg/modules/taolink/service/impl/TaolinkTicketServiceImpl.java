package org.jeecg.modules.taolink.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.taolink.entity.TaolinkTicket;
import org.jeecg.modules.taolink.mapper.TaolinkTicketMapper;
import org.jeecg.modules.taolink.service.ITaolinkTicketService;
import org.springframework.stereotype.Service;

@Service
public class TaolinkTicketServiceImpl extends ServiceImpl<TaolinkTicketMapper, TaolinkTicket> implements ITaolinkTicketService {
}

