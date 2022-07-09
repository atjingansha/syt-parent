package com.atguigu.yygh.controller;

import com.atguigu.common.handler.YughException;
import com.atguigu.model.hosp.HospitalSet;
import com.atguigu.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.service.HospitalSetService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-15 9:37
 */

@RestController
@RequestMapping("/admin/hosp/hospitalSet")
@Api(description = "医院设置接口")
/*@CrossOrigin*/
public class HospitalSetController {

    @Autowired
    private HospitalSetService hospitalSetService;






    @ApiOperation("模拟登录")
    @PostMapping("/login")
    public R login(){
        return R.ok().data("token","admin-token");
    }


    @ApiOperation("模拟获取用户信息")
    @GetMapping("/info")
    public R info(){
        Map<String,Object> map = new HashMap<>();
        map.put("roles","admin");
        map.put("introduction","I am a super administrator");
        map.put("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
        map.put("name","Super Admin");
        return R.ok().data(map);
    }















    @ApiOperation("查询所有医院设置")
    @GetMapping("findAll")
    public R findAll() {
        List<HospitalSet> list = hospitalSetService.list();
        //try {
        //    int num=  1/0;
        //} catch (Exception e) {
        //    e.printStackTrace();
        //    throw new YughException(2001,"自定义异常处理");
        //}
        return R.ok().data("list",list);
    }

    @ApiOperation("删除医院设置")
    @DeleteMapping("/{id}")
    public R delete(@PathVariable("id") Long id){
        hospitalSetService.removeById(id);
        return R.ok();
    }

    @ApiOperation("分页查询")
    @GetMapping("findPage/{page}/{limit}")
    public R findPage(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable("page") Long page,

            @ApiParam(name = "limit", value = "每页显示几条数据", required = true)
            @PathVariable("limit") Long limit){


        Page<HospitalSet> initPage=new Page<>(page,limit);

        hospitalSetService.page(initPage,null);

        List<HospitalSet> records = initPage.getRecords();
        long total = initPage.getTotal();

        return R.ok().data("records",records).data("total",total);
    }

    @ApiOperation("带条件分页查询医院设置")
    @PostMapping("findPageVo/{page}/{limit}")
    public R findPageVo(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable("page") Long page,

            @ApiParam(name = "limit", value = "每页显示几条数据", required = true)
            @PathVariable("limit") Long limit,

            @ApiParam(name = "hospitalQueryVo", value = "查询条件")
            @RequestBody HospitalQueryVo hospitalQueryVo
            ){

        String hosname = hospitalQueryVo.getHosname();
        String hoscode = hospitalQueryVo.getHoscode();

        Page<HospitalSet> pageData=new Page<>(page,limit);

        QueryWrapper<HospitalSet> queryWrapper=new QueryWrapper<>();

        if (!StringUtils.isEmpty(hosname)){
            queryWrapper.like("hosname",hosname);
        }
        if (!StringUtils.isEmpty(hoscode)){
            queryWrapper.eq("hoscode",hoscode);
        }

        hospitalSetService.page(pageData,queryWrapper);

        long total = pageData.getTotal();
        List<HospitalSet> records = pageData.getRecords();

        return R.ok().data("total",total).data("records",records);

    }

    @ApiOperation("添加医院设置")
    @PostMapping("/save")
    public R save(
            @ApiParam(name ="hospitalSet",value = "添加医院设置对象")
            @RequestBody HospitalSet hospitalSet){

        boolean save = hospitalSetService.save(hospitalSet);

        if(save){
            return R.ok();
        }else {
            return R.error();
        }
    }


    @ApiOperation("修改医院设置回显查询")
    @GetMapping("/getHosp/{id}")
    public R getHosp(@PathVariable Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);

        return R.ok().data("hospitalSet",hospitalSet);
    }

    @ApiOperation("添加医院设置")
    @PutMapping("/update")
    public R update(
            @ApiParam(name ="hospitalSet",value = "修改医院设置对象")
            @RequestBody HospitalSet hospitalSet){

        boolean update = hospitalSetService.updateById(hospitalSet);

        if(update){
            return R.ok();
        }else {
            return R.error();
        }
    }

    @ApiOperation("批量删除医院设置")
    @DeleteMapping("/bacthDelete")
    public R bacthDelete(@RequestBody List<Long> idList){

        boolean remove = hospitalSetService.removeByIds(idList);
        if(remove){
            return R.ok();
        }else {
            return R.error();
        }

    }

    @ApiOperation("医院设置锁定和解锁")
    @PutMapping("/lockHsop/{id}/{status}")
    public R lockHsop(
            @PathVariable Long id,
            @PathVariable Integer status){

        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        boolean update = hospitalSetService.updateById(hospitalSet);
        if(update){
            return R.ok();
        }else {
            return R.error();
        }
    }

}
