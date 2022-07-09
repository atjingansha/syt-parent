package com.atguigu.yygh.user.client;


import com.atguigu.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author WangJin
 * @create 2022-07-07 15:15
 */
@FeignClient("service-user")
@Repository
public interface PatientFeignClient {

    //获取就诊人信息
    @GetMapping("/api/user/patient/inner/get/{id}")
    Patient getPatient(@PathVariable("id") Long id);
}
