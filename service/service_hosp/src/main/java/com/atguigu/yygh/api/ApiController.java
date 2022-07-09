package com.atguigu.yygh.api;

import com.atguigu.common.handler.YughException;
import com.atguigu.model.hosp.Department;
import com.atguigu.model.hosp.Hospital;
import com.atguigu.model.hosp.Schedule;
import com.atguigu.vo.hosp.DepartmentQueryVo;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.common.Result;
import com.atguigu.yygh.service.DepartmentService;
import com.atguigu.yygh.service.HospitalService;
import com.atguigu.yygh.service.HospitalSetService;
import com.atguigu.yygh.service.ScheduleService;
import com.atguigu.yygh.utils.HttpRequestHelper;
import com.atguigu.yygh.utils.MD5;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-24 10:27
 */
@Api(tags = "医院管理API接口")
@RestController
@RequestMapping("/api/hosp")
public class ApiController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation("保存医院信息")
    @PostMapping("saveHospital")
    public Result saveHospital(HttpServletRequest request){

        //1.获取并转化参数
        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);
        //2.签名校验

        String signKey = hospitalSetService.getSignKey(request.getParameter("hoscode"));
        String encrypt = MD5.encrypt(signKey);

        System.out.println(request.getParameter("sign"));
        System.out.println(encrypt);


        if (!encrypt.equals(request.getParameter("sign"))){
            throw new YughException(20001,"签名校验失败");
        }


        //传输过程中"+"转换为了" ",需要转回来
        String logoData = request.getParameter("logoData");
        logoData =logoData.replaceAll(" ","+");
        paramMap.put("logoData",logoData);

        //3.调用方法存储数据
        hospitalService.save(paramMap);
        //4.返回结果
        return Result.ok();
    }

    @ApiOperation("查询医院")
    @PostMapping("/hospital/show")
    public Result show(HttpServletRequest request){

        String hoscode = request.getParameter("hoscode");
        String sign = request.getParameter("sign");

        String signKey = hospitalSetService.getSignKey(hoscode);

        String encrypt = MD5.encrypt(signKey);

       Hospital hospital= hospitalService.getByHoscode(hoscode);

        if (hospital==null){
            throw new YughException(20002,"查询失败");
        }

        return Result.ok(hospital);
    }

    @ApiOperation("上传科室")
    @PostMapping("/saveDepartment")
    public Result saveDept(HttpServletRequest request){

        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);


        departmentService.saveDept(paramMap);

        return Result.ok();
    }

    @ApiOperation("带条件,带分页,查询科室")
    @PostMapping("/department/list")
    public Result getDept(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);

        String hoscode = (String) paramMap.get("hoscode");

        int  page= StringUtils.isEmpty(request.getParameter("page"))?1:Integer.parseInt(request.getParameter("page"));
     int  limit= StringUtils.isEmpty(request.getParameter("limit"))?10:Integer.parseInt(request.getParameter("limit"));

        DepartmentQueryVo departmentQueryVo=new DepartmentQueryVo();
        departmentQueryVo.setHoscode(hoscode);


        Page<Department> pageModel= departmentService.selectPage(page,limit,departmentQueryVo);

        return Result.ok(pageModel);

    }

    @ApiOperation("删除科室")
    @PostMapping("/department/remove")
    public Result delDept(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);

        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");

        departmentService.delDept(hoscode,depcode);

        return Result.ok();
    }

    @ApiOperation("排班查询")
    @PostMapping("/schedule/list")
    public Result getSchedule(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);

        String hoscode = (String) paramMap.get("hoscode");

        int  page= StringUtils.isEmpty(request.getParameter("page"))?1:Integer.parseInt(request.getParameter("page"));
        int  limit= StringUtils.isEmpty(request.getParameter("limit"))?10:Integer.parseInt(request.getParameter("limit"));

        ScheduleOrderVo scheduleOrderVo=new ScheduleOrderVo();

        scheduleOrderVo.setHoscode(hoscode);


        Page<Schedule> pageModel = scheduleService.getSchedule(page, limit, scheduleOrderVo);
        return Result.ok(pageModel);
    }

    @ApiOperation("上传排班")
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){

        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);



        scheduleService.saveSchedule(paramMap);

        return Result.ok();
    }

    @ApiOperation("删除科室")
    @PostMapping("/schedule/remove")
    public Result remove(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();

        Map<String, Object> paramMap = HttpRequestHelper.switchMap(parameterMap);


        String hoscode = (String) paramMap.get("hoscode");
        String hosScheduleId = (String) paramMap.get("hosScheduleId");

        scheduleService.delSchedule(hoscode,hosScheduleId);

        return Result.ok();
    }
}