package com.atguigu.yygh.order.service.impl;

import com.atguigu.common.handler.YughException;
import com.atguigu.enums.OrderStatusEnum;
import com.atguigu.enums.PaymentStatusEnum;
import com.atguigu.model.order.OrderInfo;
import com.atguigu.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-07-08 15:12
 */
@Service
public class PaymentServiceImpl extends
        ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Autowired
    private OrderService orderService;

    //保存交易记录
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        //1.根据订单id+paymentType,查询有无交易记录
        QueryWrapper<PaymentInfo> wrapper=new QueryWrapper<>();

        wrapper.eq("order_id",orderInfo.getId());
        wrapper.eq("payment_type",paymentType);

        Integer count = baseMapper.selectCount(wrapper);

        //2.有记录返回,无记录插入
        if (count>0){
            return;
        }else {
            PaymentInfo paymentInfo=new PaymentInfo();
            paymentInfo.setCreateTime(new Date());
            paymentInfo.setOrderId(orderInfo.getId());
            paymentInfo.setPaymentType(paymentType);
            paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
            paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
            String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
            paymentInfo.setSubject(subject);
            paymentInfo.setTotalAmount(orderInfo.getAmount());
            baseMapper.insert(paymentInfo);
        }

    }

    /**支付成功后更新数据*/
    @Override
    public void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> resultMap) {

        //1.根据条件查询交易记录,状态判断
        PaymentInfo paymentInfo=this.getPaymentInfo(outTradeNo,paymentType);

        if (paymentInfo==null){
            throw new YughException(20001,"交易记录失效");
        }
        if (paymentInfo.getPaymentStatus()!=PaymentStatusEnum.UNPAID.getStatus()){
           return;
        }
        //2.更新交易记录
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(resultMap.toString());
        baseMapper.updateById(paymentInfo);

        //3.更新订单记录
        OrderInfo orderInfo=orderService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);


        //4.调用医院接口更新状态
    }

    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }

    private PaymentInfo getPaymentInfo(String outTradeNo, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper=new QueryWrapper<>();

        wrapper.eq("out_trade_no",outTradeNo);
        wrapper.eq("payment_type",paymentType);

        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);

        return paymentInfo;
    }
}
