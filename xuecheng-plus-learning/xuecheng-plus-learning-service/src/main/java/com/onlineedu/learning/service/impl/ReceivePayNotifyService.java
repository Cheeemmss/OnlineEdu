package com.onlineedu.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.messagesdk.model.po.MqMessage;
import com.onlineedu.learning.config.PayNotifyConfiger;
import com.onlineedu.learning.mapper.XcChooseCourseMapper;
import com.onlineedu.learning.service.MyCourseTablesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * 接收支付结果
 */
@Slf4j
@Service
public class ReceivePayNotifyService {

    @Resource
    private XcChooseCourseMapper chooseCourseMapper;

    @Resource
    private MyCourseTablesService courseTablesService;


    //监听消息队列接收支付结果通知
    @RabbitListener(queues = PayNotifyConfiger.PAYNOTIFY_QUEUE)
    public void receive(Message message) throws BusinessException {
        byte[] body = message.getBody();
        String jsonMessage = new String(body);
        MqMessage mqMessage = JSON.parseObject(jsonMessage, MqMessage.class);
        log.info("接收到消息:{}",mqMessage.toString());
        String messageType = mqMessage.getMessageType();
        String orderType = mqMessage.getBusinessKey2();
        //只处理MESSAGE_TYPE为支付结果通知的message 并且订单类型为购买课程
        if(PayNotifyConfiger.MESSAGE_TYPE.equals(messageType) && "60201".equals(orderType)){
            String chooseCourseId = mqMessage.getBusinessKey1();
            //修改选课状态为已支付 并且加入到我的课程表
            courseTablesService.saveChooseCourseSuccess(chooseCourseId);
        }
    }


}
