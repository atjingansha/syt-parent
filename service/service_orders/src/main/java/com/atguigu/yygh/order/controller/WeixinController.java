package com.atguigu.yygh.order.controller;

/**
 * @author WangJin
 * @create 2022-07-08 15:26
 */

import com.atguigu.enums.PaymentTypeEnum;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeixinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/order/weixin")
@Api(tags="微信支付接口")
public class WeixinController {

@Autowired
    private WeixinService weixinService;

@Autowired
private PaymentService paymentService;

    @GetMapping("/createNative/{orderId}")
    public R createNative(@PathVariable("orderId") Long orderId) {
        Map map = weixinService.createNative(orderId);

        return R.ok().data(map);
    }


    @ApiOperation(value = "查询支付状态")
    @GetMapping("/queryPayStatus/{orderId}")
    public R queryPayStatus(@PathVariable Long orderId){
        //1.调用微信接口查询获得结果
        Map<String,String> resultMap=
                weixinService.queryPayStatus(orderId, PaymentTypeEnum.WEIXIN.getStatus());
        //2.判断支付失败
        if (resultMap==null){
            return R.error().message("微信支付失败");
        }
        //3.判断支付成功
        if ("SUCCESS".equals(resultMap.get("trade_state"))){
            String outTradeNo = resultMap.get("out_trade_no");
            paymentService.paySuccess(outTradeNo,PaymentTypeEnum.WEIXIN.getStatus(),resultMap);

            return R.ok().message("支付成功");
        }
        //4.支付中
        return R.ok().message("支付中");

    }

}
