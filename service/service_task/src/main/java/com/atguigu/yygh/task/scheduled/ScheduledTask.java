package com.atguigu.yygh.task.scheduled;

import com.atguigu.yygh.common.service.MqConst;
import com.atguigu.yygh.common.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author WangJin
 * @create 2022-07-09 14:23
 */
@Component
@EnableScheduling
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    //@Scheduled(cron = "0/3 * * * * ? ")
    //public void test(){
    //    System.out.println("定时任务执行");
    //}

    @Scheduled(cron = "0 0 0,8 * * ? ")
    public void test(){
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8,"就医提醒,模拟消息发送");
    }
}
