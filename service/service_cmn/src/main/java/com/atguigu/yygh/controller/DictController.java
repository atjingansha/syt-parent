package com.atguigu.yygh.controller;

import com.atguigu.model.cmn.Dict;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author WangJin
 * @create 2022-06-21 9:33
 */
@RestController
@RequestMapping("/admin/cmn/dict")
/*@CrossOrigin*/
@Api(description = "数据字典接口")
public class DictController {


    @Autowired
    private DictService dictService;


    @ApiOperation("根据id查询子节点数据")
    @GetMapping("findChildData/{id}")
    public R findChildData(@PathVariable Long id) {
        List<Dict> list = dictService.findChlidData(id);
        return R.ok().data("list",list);
    }

    @ApiOperation("根据dictCode获取下级数据")
    @GetMapping("/findByDictcode/{dictCode}")
    public R findByDictcode(@PathVariable String dictCode) {
        List<Dict> list = dictService.findByDictcode(dictCode);
        return R.ok().data("list",list);
    }

    @ApiOperation("导出数据")
    @GetMapping("/export")
    public void export(HttpServletResponse response) {
        dictService.export(response);
    }


    @ApiOperation("导入数据")
    @PostMapping("/import")
    public R importData(MultipartFile file){

        dictService.importData(file);

        return R.ok();
    }


    @ApiOperation(value = "获取数据字典名称(自定义)")
    @GetMapping(value = "/getName/{parentDictCode}/{value}")
    public String getName(@PathVariable("parentDictCode") String parentDictCode,
                           @PathVariable("value") String value){

      String name=  dictService.getName(parentDictCode,value);

      return name;
    }

    @ApiOperation(value = "获取数据字典名称(国标)")
    @GetMapping(value = "/getName/{value}")
    public String getName(@PathVariable("value") String value){

        String name=  dictService.getName("",value);

        return name;
    }

}
