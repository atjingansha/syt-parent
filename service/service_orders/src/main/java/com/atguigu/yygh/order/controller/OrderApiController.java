package com.atguigu.yygh.order.controller;

import com.atguigu.enums.OrderStatusEnum;
import com.atguigu.model.order.OrderInfo;
import com.atguigu.vo.order.OrderCountQueryVo;
import com.atguigu.vo.order.OrderQueryVo;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.order.service.OrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-07-07 14:34
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "创建订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public R submitOrder(
            @PathVariable String scheduleId,
            @PathVariable Long patientId) {
        Long orderId = orderService.submitOrder(scheduleId, patientId);

        return R.ok().data("orderId", orderId);
    }

    @ApiOperation("带条件带分页查询订单")
    @GetMapping("auth/{page}/{limit}")
    public R list(@PathVariable Long page,
                  @PathVariable Long limit,
                  OrderQueryVo orderQueryVo, HttpServletRequest request) {

        //获取userId,封装到vo中
        Long userId = AuthContextHolder.getUserId(request);

        orderQueryVo.setUserId(userId);

        //创建分页查询对象
        Page<OrderInfo> pageParam = new Page<>(page, limit);

        Page<OrderInfo> pageModel = orderService.selectPage(pageParam, orderQueryVo);

        return R.ok().data("pageModel", pageModel);
    }


    @ApiOperation(value = "获取订单状态")
    @GetMapping("auth/getStatusList")
    public R getStatusList() {
        return R.ok().data("statusList", OrderStatusEnum.getStatusList());
    }



    @ApiOperation(value = "根据订单id查询订单详情")
    @GetMapping("auth/getOrders/{orderId}")
    public R getOrders(@PathVariable Long orderId){
        OrderInfo orderInfo=orderService.getOrderInfo(orderId);

        return R.ok().data("orderInfo",orderInfo);
    }

    @ApiOperation(value = "取消预约")
    @GetMapping("auth/cancelOrder/{orderId}")
    public R cancelOrder(
            @ApiParam(name = "orderId", value = "订单id", required = true)
            @PathVariable("orderId") Long orderId) {

        Boolean flag = orderService.cancelOrder(orderId);
        return R.ok().data("flag",flag);
    }

    @ApiOperation("统计订单数据")
    @PostMapping("inner/getCountMap")
    public Map<String,Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo){
        Map<String,Object> map=orderService.getCountMap(orderCountQueryVo);

        return map;
    }


}

