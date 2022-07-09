package com.atguigu.yygh.user.service;

import com.atguigu.model.user.Patient;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author WangJin
 * @create 2022-07-03 16:32
 */
public interface PatientService extends IService<Patient> {

    //获取就诊人数列表
    List<Patient> findAll(Long userId);

    Patient getPatientById(Long id);
}
