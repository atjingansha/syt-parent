package com.atguigu.yygh.receiver;

import com.atguigu.model.hosp.Schedule;
import com.atguigu.vo.msm.MsmVo;
import com.atguigu.vo.order.OrderMqVo;
import com.atguigu.yygh.common.service.MqConst;
import com.atguigu.yygh.common.service.RabbitService;
import com.atguigu.yygh.service.ScheduleService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.channels.Channel;

/**
 * @author WangJin
 * @create 2022-07-08 10:41
 */
@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_ORDER,durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_ORDER),
            key = {MqConst.ROUTING_ORDER}
    ))
    public void  receiver(OrderMqVo orderMqVo, Message message, Channel channel){

        String hoscode = orderMqVo.getHoscode();
        String scheduleId = orderMqVo.getScheduleId();

        //查询排班信息
      Schedule schedule= scheduleService.getScheduleByInfo(hoscode,scheduleId);

        Integer availableNumber = orderMqVo.getAvailableNumber();
        Integer reservedNumber = orderMqVo.getReservedNumber();

        //判断创建订单还是取消预约
        if (StringUtils.isEmpty(availableNumber)){
            //取消预约
            availableNumber= schedule.getAvailableNumber()+1;
            schedule.setAvailableNumber(availableNumber);
        }else {
            //创建订单
            schedule.setReservedNumber(reservedNumber);
            schedule.setAvailableNumber(availableNumber);
        }

        scheduleService.update(schedule);

        //发送短信消息
        MsmVo msmVo = orderMqVo.getMsmVo();
        if(null != msmVo) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }
}
