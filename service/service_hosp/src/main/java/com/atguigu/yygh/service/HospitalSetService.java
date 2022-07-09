package com.atguigu.yygh.service;

import com.atguigu.model.hosp.HospitalSet;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author WangJin
 * @create 2022-06-15 9:34
 */
public interface HospitalSetService extends IService<HospitalSet> {


    /**
     * 根据医院编码获取签名
     * @param hospcode
     * @return
     */
    String getSignKey(String hospcode);
}
