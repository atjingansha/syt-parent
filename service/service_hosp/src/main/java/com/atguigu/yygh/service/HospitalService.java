package com.atguigu.yygh.service;

import com.atguigu.model.hosp.Hospital;
import com.atguigu.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-24 10:24
 */
public interface HospitalService {
    void save(Map<String, Object> paramMap);

    Hospital getByHoscode(String hoscode);

    Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    //更新上线状态
    void updateStatus(String id, Integer status);

    //详情
    Map<String, Object> show(String id);

    //根据医院编码获取医院名称
    String getHospName(String hoscode);


    //根据医院名称获取医院列表
    List<Hospital> findByHosnameLike(String hosname);

    //根据医院编码获取详情
    Map<String, Object> getHospInfoByHoscode(String hoscode);
}
