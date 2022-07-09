package com.atguigu.yygh.controller;

import com.atguigu.vo.hosp.DepartmentVo;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.service.DepartmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author WangJin
 * @create 2022-06-27 10:31
 */
@RestController
@RequestMapping("/admin/hosp/department")
@Api(description = "科室接口")
public class DepartmentController {

    @Autowired
    DepartmentService departmentService;


    @ApiOperation(value = "查询医院所有科室列表")
    @GetMapping("getDeptList/{hoscode}")
    public R getDeptList(@PathVariable String hoscode) {
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return R.ok().data("list",list);
    }
}
