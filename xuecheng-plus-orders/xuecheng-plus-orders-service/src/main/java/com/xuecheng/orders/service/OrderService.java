package com.xuecheng.orders.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.onlineedu.base.exception.BusinessException;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcPayRecord;

/**
 * @Author cheems
 * @Date 2023/3/11 13:09
 */
public interface OrderService extends IService<XcOrders> {

    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) throws BusinessException;

    public XcPayRecord getPayRecordByPayno(String payNo);

    public PayRecordDto queryPayResult(String payNo) throws BusinessException;

    public void saveAliPayStatus(PayStatusDto payStatusDto) throws BusinessException;
}
