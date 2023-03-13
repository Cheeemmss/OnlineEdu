package com.onlineedu.orders.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.messagesdk.model.po.MqMessage;
import com.onlineedu.orders.model.dto.AddOrderDto;
import com.onlineedu.orders.model.dto.PayRecordDto;
import com.onlineedu.orders.model.dto.PayStatusDto;
import com.onlineedu.orders.model.po.XcOrders;
import com.onlineedu.orders.model.po.XcPayRecord;

/**
 * @Author cheems
 * @Date 2023/3/11 13:09
 */
public interface OrderService extends IService<XcOrders> {

    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) throws BusinessException;

    public XcPayRecord getPayRecordByPayno(String payNo);

    public PayRecordDto queryPayResult(String payNo) throws BusinessException;

    public void saveAliPayStatus(PayStatusDto payStatusDto) throws BusinessException;

    public void notifyPayResult(MqMessage message);

}
