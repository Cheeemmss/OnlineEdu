package com.xuecheng.orders.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.OrderDetail;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.exception.CommonError;
import com.onlineedu.base.model.SystemCode;
import com.onlineedu.base.utils.IdWorkerUtils;
import com.onlineedu.base.utils.QRCodeUtil;
import com.xuecheng.orders.config.AlipayConfig;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.dto.PayStatusDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.service.XcOrdersGoodsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.onlineedu.base.model.SystemCode.CODE_UNKOWN_ERROR;

/**
 * @Author cheems
 * @Date 2023/3/11 13:09
 */
@Service
public class OrderServiceImpl extends ServiceImpl<XcOrdersMapper, XcOrders> implements OrderService{

    @Resource
    private XcOrdersGoodsService ordersGoodsService;

    @Resource
    private XcPayRecordMapper payRecordMapper;

    @Resource
    private ApplicationContext applicationContext;

    @Value("${pay.alipay.APP_ID}")
    String APP_ID;

    @Value("${pay.alipay.APP_PRIVATE_KEY}")
    String APP_PRIVATE_KEY;

    @Value("${pay.alipay.ALIPAY_PUBLIC_KEY}")
    String ALIPAY_PUBLIC_KEY;

    @Value("${QR.request_url}")
    private String QRRequestUrl;

    /**
     * 创建订单
     * @param userId
     * @param addOrderDto
     * @return 支付记录 + 二维码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) throws BusinessException {
        //保存订单信息
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);
        //保存支付记录
        XcPayRecord payRecord = createPayRecord(xcOrders);
        //生成二维码
        String QRCode = "";
        try {
            QRCode = new QRCodeUtil().createQRCode( QRRequestUrl + payRecord.getPayNo(), 200, 200);
        } catch (IOException exception) {
            throw new BusinessException(CODE_UNKOWN_ERROR,"生成支付二维码错误");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtil.copyProperties(payRecord,payRecordDto);
        payRecordDto.setQrcode(QRCode);
        return payRecordDto;
    }

    /**
     * 保存订单信息
     * @param userId
     * @param addOrderDto
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto) throws BusinessException {
        LambdaQueryWrapper<XcOrders> wrapper = new LambdaQueryWrapper<XcOrders>()
                .eq(XcOrders::getOutBusinessId, addOrderDto.getOutBusinessId());
        XcOrders one = getOne(wrapper);
        //创建订单之前会向选课记录表添加一条数据 若课程记录表已存在记录 则不重复创建订单 (幂等性)
        if(one != null){
            return one;
        }
        //保存到订单表
        XcOrders xcOrders = new XcOrders();
        long orderId = IdWorkerUtils.getInstance().nextId(); //雪花算法生成订单Id
        xcOrders.setId(orderId);
        xcOrders.setTotalPrice(addOrderDto.getTotalPrice());
        xcOrders.setCreateDate(LocalDateTime.now());
        xcOrders.setStatus("600001"); //订单状态未支付
        xcOrders.setUserId(userId);
        xcOrders.setOrderName(addOrderDto.getOrderName());
        xcOrders.setOrderType(addOrderDto.getOrderType());
        xcOrders.setOrderDescrip(addOrderDto.getOrderDescrip());
        xcOrders.setOrderDetail(addOrderDto.getOrderDetail());
        xcOrders.setOutBusinessId(addOrderDto.getOutBusinessId());
        boolean save = save(xcOrders);
        if(!save){
            throw new BusinessException(CODE_UNKOWN_ERROR,"保存订单失败");
        }
        //保存到订单明细表
        List<XcOrdersGoods> xcOrdersGoods = JSON.parseArray(addOrderDto.getOrderDetail(), XcOrdersGoods.class);
        xcOrdersGoods.forEach(e -> e.setOrderId(orderId));
        boolean saveBatch = ordersGoodsService.saveBatch(xcOrdersGoods);
        if(!saveBatch){
            throw new BusinessException(CODE_UNKOWN_ERROR,"保存订单明细失败");
        }
        return xcOrders;
    }

    /**
     * 保存支付记录
     * @param orders 上一步保存的订单信息
     * @return
     */
    public XcPayRecord createPayRecord(XcOrders orders) throws BusinessException {
        XcPayRecord xcPayRecord = new XcPayRecord();
        long payNo = IdWorkerUtils.getInstance().nextId();
        xcPayRecord.setPayNo(payNo); //系统内部支付记录流水号
        xcPayRecord.setOrderId(orders.getId());
        xcPayRecord.setOrderName(orders.getOrderName());
        xcPayRecord.setTotalPrice(orders.getTotalPrice());
        xcPayRecord.setCurrency("CNY"); //币种
        xcPayRecord.setCreateDate(LocalDateTime.now());
        xcPayRecord.setStatus("601001");           //支付记录状态未支付
        xcPayRecord.setUserId(orders.getUserId());
        int insert = payRecordMapper.insert(xcPayRecord);
        if(insert <= 0){
            throw new BusinessException(CODE_UNKOWN_ERROR,"保存支付记录失败");
        }
        return xcPayRecord;
    }

    @Override
    public XcPayRecord getPayRecordByPayno(String payNo) {
        XcPayRecord xcPayRecord = payRecordMapper.selectOne(new LambdaQueryWrapper<XcPayRecord>().eq(XcPayRecord::getPayNo, payNo));
        return xcPayRecord;
    }

    /**
     * 主动查询支付结果
     * @param payNo 系统支付记录流水号
     * @return
     */
    @Override
    public PayRecordDto queryPayResult(String payNo) throws BusinessException {
        //查询支付宝获取订单支付交易情况
        PayStatusDto payStatusDto = queryPayResultFromAlipay(payNo);
        //更新订单表 支付记录表
        OrderService orderService = applicationContext.getBean(OrderService.class);
        orderService.saveAliPayStatus(payStatusDto);
        XcPayRecord payRecord = getPayRecordByPayno(payNo);
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtil.copyProperties(payRecord,payRecordDto);
        return payRecordDto;
    }


    /**
     * 请求支付宝获取支付结果
     * @param payNo 系统支付记录流水号
     * @return 支付结果
     */
    public PayStatusDto queryPayResultFromAlipay(String payNo) throws BusinessException {
        XcPayRecord xcPayRecord = getPayRecordByPayno(payNo);
        if(xcPayRecord == null){
            throw new BusinessException(CODE_UNKOWN_ERROR,"支付记录不存在");
        }
        //请求支付宝获取支付订单信息
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.URL, APP_ID, APP_PRIVATE_KEY, "json", AlipayConfig.CHARSET, ALIPAY_PUBLIC_KEY, AlipayConfig.SIGNTYPE); //获得初始化的AlipayClient
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", payNo);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
            if (!response.isSuccess()) {
                throw new BusinessException(CODE_UNKOWN_ERROR,"请求支付查询查询失败");
            }
        } catch (AlipayApiException e) {
            log.error("请求支付宝查询支付结果异常:{}",e);
            throw new BusinessException(CODE_UNKOWN_ERROR,"请求支付查询查询失败");
        }
        //组装Dto
        Map resultMap = JSON.parseObject(response.getBody(), Map.class);
        Map tradeQueryResponse = (Map) resultMap.get("alipay_trade_query_response");
        String tradeNo = (String) tradeQueryResponse.get("trade_no");
        String totalAmount = (String) tradeQueryResponse.get("total_amount");
        String tradeStatus = (String) tradeQueryResponse.get("trade_status");
        PayStatusDto payStatusDto = new PayStatusDto();
        payStatusDto.setOut_trade_no(payNo);  //注意这个Dto中的out_trade_no 为我们系统自己的支付记录流水号
        payStatusDto.setTrade_no(tradeNo);    //Dto中的trade_no为支付宝自己的交易流水号
        payStatusDto.setTrade_status(tradeStatus);
        payStatusDto.setApp_id(APP_ID);
        payStatusDto.setTotal_amount(totalAmount);
        return payStatusDto;
    }

    /**
     * 保存支付结果到 订单表 + 支付记录表
     * @param payStatusDto 查询支付宝得到的支付结果
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAliPayStatus(PayStatusDto payStatusDto) throws BusinessException {
        String outTradeNo = payStatusDto.getOut_trade_no();
        XcPayRecord payRecord = getPayRecordByPayno(outTradeNo);
        if(payRecord == null){
            throw new BusinessException(CODE_UNKOWN_ERROR,"订单支付记录不存在");
        }
        Long orderId = payRecord.getOrderId();
        XcOrders xcOrders = getById(orderId);
        if(xcOrders == null){
            throw new BusinessException(CODE_UNKOWN_ERROR,"订单不存在");
        }
        //支付记录为已支付过就不用再支付了
        if("601002".equals(xcOrders.getStatus())){
            return;
        }

        //支付成功(从支付宝获取的交易状态为 TRADE_SUCCESS)
        if("TRADE_SUCCESS".equals(payStatusDto.getTrade_status())){
            //跟新支付记录表
            payRecord.setOutPayNo(payStatusDto.getTrade_no());
            payRecord.setOutPayChannel("AliPay");
            payRecord.setStatus("601002");
            payRecord.setPaySuccessTime(LocalDateTime.now());
            int update = payRecordMapper.updateById(payRecord);
            if(update <= 0){
                throw new BusinessException(CommonError.UPDATE_EXCEPTION);
            }
            //跟新订单表
            xcOrders.setStatus("601002");
            boolean b = updateById(xcOrders);
            if(!b){
                throw new BusinessException(CommonError.UPDATE_EXCEPTION);
            }

        }
    }
}
