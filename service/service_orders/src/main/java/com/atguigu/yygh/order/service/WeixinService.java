package com.atguigu.yygh.order.service;

import java.util.Map;

/**
 * @author WangJin
 * @create 2022-07-08 15:29
 */
public interface WeixinService {

    /**生成二维码*/
    Map createNative(Long orderId);

    /**查询支付状态*/
    Map<String, String> queryPayStatus(Long orderId, Integer paymentType);

    /**微信退款*/
    Boolean refund(Long orderId);
}
