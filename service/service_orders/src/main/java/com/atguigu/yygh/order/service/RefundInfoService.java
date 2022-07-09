package com.atguigu.yygh.order.service;

import com.atguigu.model.order.PaymentInfo;
import com.atguigu.model.order.RefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author WangJin
 * @create 2022-07-09 9:50
 */
public interface RefundInfoService extends IService<RefundInfo> {
    /**
     * 保存退款记录
     * @param paymentInfo
     */
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);
}
