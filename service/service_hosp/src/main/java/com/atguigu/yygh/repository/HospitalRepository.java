package com.atguigu.yygh.repository;

import com.atguigu.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author WangJin
 * @create 2022-06-24 10:22
 */
@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {

    Hospital getByHoscode(String hoscode);

    //根据医院名称获取医院列表
    List<Hospital> getByHosnameLike(String hosname);
}