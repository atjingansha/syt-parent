package com.atguigu.yygh.repository;

import com.atguigu.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author WangJin
 * @create 2022-06-24 14:27
 */
@Repository
public interface DepartmentRepository extends MongoRepository<Department,String> {


    /**
     * 根据医院编号和科室编号查询部门
     * @param hoscode
     * @param depcode
     */
 public  Department   getDeptByHoscodeAndDepcode(String hoscode,String depcode);

    List<Department> getByHoscode(String hoscode);
}