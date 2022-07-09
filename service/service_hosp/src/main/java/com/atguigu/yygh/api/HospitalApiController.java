package com.atguigu.yygh.api;

import com.atguigu.model.hosp.Hospital;
import com.atguigu.model.hosp.Schedule;
import com.atguigu.vo.hosp.DepartmentVo;
import com.atguigu.vo.hosp.HospitalQueryVo;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.service.DepartmentService;
import com.atguigu.yygh.service.HospitalService;
import com.atguigu.yygh.service.ScheduleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-28 11:14
 */
@Api(tags = "医院显示接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {
    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "带条件获取分页列表")
    @GetMapping("{page}/{limit}")
    public R index(
            @PathVariable Integer page,
            @PathVariable Integer limit,
            HospitalQueryVo hospitalQueryVo) {

        Page<Hospital> pages=hospitalService.selectPage(page,limit,hospitalQueryVo);

        return R.ok().data("pages",pages);
    }


    @ApiOperation(value = "根据医院名称获取医院列表")
    @GetMapping("findByHosname/{hosname}")
    public R findByHosname(
            @ApiParam(name = "hosname", value = "医院名称", required = true)
            @PathVariable String hosname) {
        List<Hospital> list=hospitalService.findByHosnameLike(hosname);

        return R.ok().data("list",list);
    }

    @ApiOperation(value = "获取科室列表")
    @GetMapping("department/{hoscode}")
    public R index(
            @PathVariable String hoscode) {

        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);

        return R.ok().data("list",list);
    }

    @ApiOperation(value = "医院详情")
    @GetMapping("{hoscode}")
    public R item(@PathVariable String hoscode) {

        Map<String, Object> map = hospitalService.getHospInfoByHoscode(hoscode);
        return R.ok().data(map);
    }

    @ApiOperation(value = "获取可预约排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getBookingSchedule(@PathVariable Integer page,
                                @PathVariable Integer limit,
                                @PathVariable String hoscode,
                                @PathVariable String depcode) {
        Map<String,Object> map=scheduleService.getBookigSchedule(page,limit,hoscode,depcode);

        return R.ok().data(map);
    }

    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public R findScheduleList(
            @PathVariable String hoscode,
            @PathVariable String depcode,
            @PathVariable String workDate) {
        List<Schedule> scheduleList=scheduleService.getScheduleDetails(hoscode,depcode,workDate);

        return R.ok().data("scheduleList",scheduleList);
    }


    @ApiOperation(value = "根据排班id获取排班详情")
    @GetMapping("getSchedule/{id}")
    public R findScheduleById(@PathVariable String id ){
        Schedule schedule = scheduleService.findScheduleById(id);
        return R.ok().data("schedule",schedule);
    }

    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId ){
        ScheduleOrderVo scheduleOrderVo = scheduleService.getScheduleOrderVo(scheduleId);
        return scheduleOrderVo;
    }
}
