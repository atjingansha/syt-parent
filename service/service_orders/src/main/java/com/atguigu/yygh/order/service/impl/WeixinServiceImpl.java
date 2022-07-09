package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.handler.YughException;
import com.atguigu.enums.PaymentTypeEnum;
import com.atguigu.enums.RefundStatusEnum;
import com.atguigu.model.order.OrderInfo;
import com.atguigu.model.order.PaymentInfo;
import com.atguigu.model.order.RefundInfo;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.ConstantPropertiesUtils;
import com.atguigu.yygh.order.utils.HttpClient;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author WangJin
 * @create 2022-07-08 15:30
 */
@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RefundInfoService refundInfoService;
    /**
     *生成二维码
     * @param orderId
     * @return
     */
    @Override
    public Map createNative(Long orderId) {
        try {
            //1.根据orderId查询订单
            OrderInfo orderInfo = orderService.getById(orderId);

            if (orderInfo == null) {
                throw new YughException(20001, "订单有误");
            }
            //2.生成交易记录
            paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
            //3.封装调用微信接口参数

            //1、设置参数
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = orderInfo.getReserveDate() + "就诊" + orderInfo.getDepname();
            paramMap.put("body", body);

            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());

            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");

            paramMap.put("total_fee", "1");//为了测试

            paramMap.put("spbill_create_ip", "127.0.0.1");

            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");

            paramMap.put("trade_type", "NATIVE");
            //4.创建客户端指定url
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");

            //5.存入请求参数

            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            //6.发送请求

            client.setHttps(true);
            client.post();

            //7.拿到响应
            String content = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            System.out.println("resultMap = " + resultMap);
            //4、封装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));
            if(null != resultMap.get("result_code")) {
                //微信支付二维码2小时过期，可采取2小时未支付取消订单
                redisTemplate.opsForValue().set(orderId.toString(), map, 1000, TimeUnit.MINUTES);
            }
            return map;

        }catch (Exception e){
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**查询支付状态*/
    @Override
    public Map<String, String> queryPayStatus(Long orderId, Integer paymentType) {

        try {
            //根据orderId查询订单
            OrderInfo orderInfo = orderService.getById(orderId);

            //封装查询条件
            Map paramMap=new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //3创建客户端，存入参数

            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap,
                    ConstantPropertiesUtils.PARTNERKEY));

            //4.发送请求

            client.setHttps(true);
            client.post();

            //拿到响应,转化类型(xml->map)
            String content = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            System.out.println("查询支付resultMap = " + resultMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**退款***/
    @Override
    public Boolean refund(Long orderId) {
        try {
            //1.根据orderId查询交易记录
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId,PaymentTypeEnum.WEIXIN.getStatus());

            if (paymentInfo==null){
                throw new YughException(20001,"交易记录有误");
            }
            //2.根据交易记录保存退款记录
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            //判断是否已退款
            if (refundInfo.getRefundStatus().intValue()==
                    RefundStatusEnum.REFUND.getStatus()){
                return true;
            }
            //3.封装微信退款参数
            Map<String,String> paramMap = new HashMap<>(8);
            paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfo.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no",paymentInfo.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfo.getOutTradeNo()); //商户退款单号

            //       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            //       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");

            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");


            String paramXml = WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY);
            //4.创建客户端url
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            //5.客户端存入参数,开启读取证书
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            //6.发送请求
            client.post();
            //7.拿响应判断是否成功
            String xml = client.getContent();
            System.out.println("xml = " + xml);

            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);

            if (resultMap!=null &&
                    WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))){
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
