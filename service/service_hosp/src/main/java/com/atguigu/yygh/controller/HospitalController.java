package com.atguigu.yygh.controller;

/**
 * @author WangJin
 * @create 2022-06-25 10:05
 */

import com.atguigu.model.hosp.Hospital;
import com.atguigu.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.service.HospitalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(description = "医院接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
/*@CrossOrigin*/
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;



    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public R index(@PathVariable Integer page,
                   @PathVariable Integer limit,
                   HospitalQueryVo hospitalQueryVo) {

        Page<Hospital> pageModel=hospitalService.selectPage(page,limit,hospitalQueryVo);
        return R.ok().data("pageModel",pageModel);
    }

    @ApiOperation("更新上线状态")
    @GetMapping("updateStatus/{id}/{status}")
    public R updateStatus(@PathVariable("id") String id,
                          @PathVariable("status") Integer status){
        hospitalService.updateStatus(id,status);

        return R.ok();

    }

    @ApiOperation("更新上线状态")
    @GetMapping("show/{id}")
    public R show(@PathVariable("id") String id){


      Map<String,Object> map= hospitalService.show(id);

        return R.ok().data(map);

    }
}
