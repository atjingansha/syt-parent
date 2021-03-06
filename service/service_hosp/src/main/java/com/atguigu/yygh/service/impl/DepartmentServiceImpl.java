package com.atguigu.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.handler.YughException;
import com.atguigu.model.hosp.Department;
import com.atguigu.model.hosp.Schedule;
import com.atguigu.vo.hosp.DepartmentQueryVo;
import com.atguigu.vo.hosp.DepartmentVo;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.repository.DepartmentRepository;
import com.atguigu.yygh.service.DepartmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * @author WangJin
 * @create 2022-06-24 20:07
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;


    @Override
    public void saveDept(Map<String, Object> paramMap) {

        String toJSONString = JSONObject.toJSONString(paramMap);

        Department department = JSONObject.parseObject(toJSONString, Department.class);

        String depcode = department.getDepcode();
        String hoscode = department.getHoscode();

        Department targetDepartment=  departmentRepository.getDeptByHoscodeAndDepcode(hoscode,depcode);

        if (targetDepartment!=null){
            department.setId(department.getId());
            department.setCreateTime(department.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(department.getIsDeleted());
            departmentRepository.save(department);
        }else {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    @Override
    public Page<Department> selectPage(int page, int limit, DepartmentQueryVo departmentQueryVo) {

        Department department=new Department();

        BeanUtils.copyProperties(departmentQueryVo,department);

        ExampleMatcher matcher=ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);

        Example<Department> example=Example.of(department,matcher);

        Sort sort=Sort.by(Sort.Direction.DESC,"createTime");

        Pageable pageable= PageRequest.of(page,limit,sort);

        Page<Department> pageModel = departmentRepository.findAll(example, pageable);

        return pageModel;
    }


    @Override
    public void delDept(String hoscode, String depcode) {

        Department department=  departmentRepository.getDeptByHoscodeAndDepcode(hoscode,depcode);

        if (department==null){
            throw new YughException(50001,"????????????,??????????????????");
        }

        departmentRepository.deleteById(department.getId());

    }

    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {

        //1.????????????????????????
        List<DepartmentVo> result=new ArrayList<>();
        //2.????????????????????????
      List<Department> departmentList= departmentRepository.getByHoscode(hoscode);
        //3.??????????????????,??????bigcode??????????????????
        Map<String,List<Department>> departmentMap=departmentList.stream().collect(
                Collectors.groupingBy(Department::getDepcode)
        );
        //4.???????????????????????????
        for (Map.Entry<String, List<Department>> entry : departmentMap.entrySet()) {
            DepartmentVo BigDepVo=new DepartmentVo();

            BigDepVo.setDepcode(entry.getKey());
            BigDepVo.setDepname(entry.getValue().get(0).getBigname());
            //5.???????????????????????????
            //5.1?????????????????????
            List<Department>  depList=   entry.getValue();
            //5.2???????????????????????????,??????????????????
            List<DepartmentVo> depVoList=new ArrayList<>();

            for (Department department : depList) {
                DepartmentVo depVo=new DepartmentVo();
                depVo.setDepcode(department.getDepcode());
                depVo.setDepname(department.getDepname());
                depVoList.add(depVo);
            }
            //6.??????????????????????????????????????????
            BigDepVo.setChildren(depVoList);


            //7.?????????????????????????????????????????????
            result.add(BigDepVo);
        }
        return result;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {

        Department department = departmentRepository.getDeptByHoscodeAndDepcode(hoscode, depcode);

        if (department==null){
            throw new YughException(20001,"???????????????");
        }
        return department.getDepname();
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        Department department = departmentRepository.getDeptByHoscodeAndDepcode(hoscode, depcode);

        return department;
    }


}
