package com.atguigu.yygh.statistics.controller;

import com.atguigu.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.order.OrderFeignClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author WangJin
 * @create 2022-07-09 20:02
 */
@RestController
@RequestMapping("/admin/statistics")
@Api(tags = "统计管理接口")
public class StatisticsController {


    @Autowired
    private OrderFeignClient orderFeignClient;

    @ApiOperation(value = "获取订单统计数据")
    @GetMapping("getCountMap")
    public R getCountMap( OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> map = orderFeignClient.getCountMap(orderCountQueryVo);
        return R.ok().data(map);
    }
}
