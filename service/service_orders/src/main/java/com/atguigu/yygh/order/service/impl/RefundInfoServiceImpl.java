package com.atguigu.yygh.order.service.impl;

import com.atguigu.enums.RefundStatusEnum;
import com.atguigu.model.order.PaymentInfo;
import com.atguigu.model.order.RefundInfo;
import com.atguigu.yygh.order.mapper.RefundInfoMapper;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author WangJin
 * @create 2022-07-09 9:51
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    /**保存退款记录*/
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        //1.查询退款记录
        QueryWrapper<RefundInfo> wrapper=new QueryWrapper<>();

        wrapper.eq("order_id",paymentInfo.getOrderId());
        wrapper.eq("payment_type",paymentInfo.getPaymentType());

        RefundInfo refundInfo = baseMapper.selectOne(wrapper);

        if (refundInfo!=null){
            return refundInfo;
        }else {
            //2.新建退款记录

            refundInfo=new RefundInfo();
            refundInfo.setCreateTime(new Date());
            refundInfo.setOrderId(paymentInfo.getOrderId());
            refundInfo.setPaymentType(paymentInfo.getPaymentType());
            refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
            refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
            refundInfo.setSubject(paymentInfo.getSubject());
            refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
            baseMapper.insert(refundInfo);
            return refundInfo;

        }

    }
}