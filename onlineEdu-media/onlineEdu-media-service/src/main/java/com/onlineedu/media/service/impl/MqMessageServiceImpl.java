package com.onlineedu.media.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.media.mapper.MqMessageMapper;
import com.onlineedu.media.model.entities.MqMessage;
import com.onlineedu.media.service.MqMessageService;
import org.springframework.stereotype.Service;

/**
* @author cheems
* @description 针对表【mq_message】的数据库操作Service实现
* @createDate 2023-02-01 23:18:27
*/
@Service
public class MqMessageServiceImpl extends ServiceImpl<MqMessageMapper, MqMessage>
    implements MqMessageService{

}




