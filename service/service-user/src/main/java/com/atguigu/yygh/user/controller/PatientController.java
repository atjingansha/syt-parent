package com.atguigu.yygh.user.controller;

import com.atguigu.model.user.Patient;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.user.service.PatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author WangJin
 * @create 2022-07-03 16:35
 */
@RestController
@Api("就诊人数接口")
@RequestMapping("/api/user/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;


    @GetMapping("auth/findAll")
    @ApiOperation("获取就诊人数列表")
    public R findAll(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);

        List<Patient> list=patientService.findAll(userId);
        return R.ok().data("list",list);
    }

    @ApiOperation("添加就诊人")
    @PostMapping("auth/save")
    public R savePatient(@RequestBody Patient patient,
                         HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);

        patientService.save(patient);

        return R.ok();
    }

    @ApiOperation("根据id获取就诊人信息")
    @GetMapping("auth/get/{id}")
    public R getPatient(@PathVariable Long id) {
        Patient patient=patientService.getPatientById(id);

        return R.ok().data("patient",patient);
    }

    @ApiOperation("根据id修改就诊人信息")
    @PostMapping("auth/update")
    public R updatePatient(@RequestBody Patient patient) {
        patientService.updateById(patient);

        return R.ok();
    }

    @ApiOperation("删除就诊人")
    @DeleteMapping("auth/remove/{id}")
    public R removePatient(@PathVariable Long id) {
        patientService.removeById(id);
        return R.ok();
    }

    @ApiOperation("获取就诊人信息(远程)")
    @GetMapping("inner/get/{id}")
    public Patient getPatientOrder(@PathVariable Long id) {
       Patient patient=patientService.getPatientById(id);
        return patient;
    }

}
