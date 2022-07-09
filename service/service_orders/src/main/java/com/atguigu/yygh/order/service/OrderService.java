package com.atguigu.yygh.order.service;

import com.atguigu.model.order.OrderInfo;
import com.atguigu.vo.order.OrderCountQueryVo;
import com.atguigu.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author WangJin
 * @create 2022-07-07 14:33
 */
public interface OrderService extends IService<OrderInfo> {

    /**创建订单*/
    Long submitOrder(String scheduleId, Long patientId);


    /**带条件带分页查询订单*/
    Page<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    /**根据订单id查询订单详情*/
    OrderInfo getOrderInfo(Long orderId);

    /**
     * 取消预约
     */
    Boolean cancelOrder(Long orderId);

    /**就诊提醒*/
    void patientTips();


    /**获取订单统计数据*/
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);


}
