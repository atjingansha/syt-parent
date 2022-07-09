package com.atguigu.yygh.order.service;

import com.atguigu.model.order.OrderInfo;
import com.atguigu.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author WangJin
 * @create 2022-07-08 15:12
 */
public interface PaymentService extends IService<PaymentInfo> {

            /**
             * 保存交易记录
             * @param orderInfo
             * @param paymentType 支付类型（2：微信 1：支付宝）
             */
    void savePaymentInfo(OrderInfo orderInfo, Integer paymentType);


    /**支付成功后更新数据*/
    void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> resultMap);


    /**获取支付交易记录*/
    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);
}
