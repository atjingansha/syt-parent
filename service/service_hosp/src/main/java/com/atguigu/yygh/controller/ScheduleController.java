package com.atguigu.yygh.controller;

import com.atguigu.model.hosp.Schedule;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.service.ScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-27 14:19
 */
@RestController
@RequestMapping("/admin/hosp/schedule")
@Api(description = "排班接口")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "查询排班统计数据")
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getScheduleRule(@PathVariable long page,
                             @PathVariable long limit,
                             @PathVariable String hoscode,
                             @PathVariable String depcode){
        Map<String,Object> map
                = scheduleService.getRuleSchedule(page,limit,hoscode,depcode);
        return R.ok().data(map);
    }

    @ApiOperation(value = "查询排班详细信息")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public R getScheduleDetails(@PathVariable String hoscode,
                                @PathVariable String depcode,
                                @PathVariable String workDate){

        List<Schedule> list=scheduleService.getScheduleDetails(hoscode,depcode,workDate);
        return R.ok().data("list",list);
    }
}
