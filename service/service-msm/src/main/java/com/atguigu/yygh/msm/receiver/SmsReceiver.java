package com.atguigu.yygh.msm.receiver;

import com.atguigu.vo.msm.MsmVo;
import com.atguigu.yygh.common.service.MqConst;
import com.atguigu.yygh.msm.service.MsmService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.channels.Channel;

/**
 * @author WangJin
 * @create 2022-07-08 10:24
 */
@Component
public class SmsReceiver {

    @Autowired
    private MsmService msmService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM,durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void  send(MsmVo msmVo, Message message, Channel channel){
        msmService.send(msmVo);
    }
}
