package com.atguigu.yygh.service;

import com.atguigu.model.hosp.Department;
import com.atguigu.model.hosp.Schedule;
import com.atguigu.vo.hosp.DepartmentQueryVo;
import com.atguigu.vo.hosp.DepartmentVo;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-24 20:07
 */
public interface DepartmentService {

    void saveDept(Map<String, Object> paramMap);

    Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo);

    void delDept(String hoscode, String depcode);


    /**查询医院所有科室列表*/
    List<DepartmentVo> findDeptTree(String hoscode);

    /**根据医院编码和科室编码获取科室名称*/
    String getDepName(String hoscode, String depcode);



    /**查询科室*/
    Department getDepartment(String hoscode, String depcode);
}
