package com.atguigu.yygh.service;

import com.atguigu.model.cmn.Dict;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author WangJin
 * @create 2022-06-21 9:30
 */
public interface DictService extends IService<Dict> {
    //根据数据id查询子数据列表
    List<Dict> findChlidData(Long id);

    //导出数据
    void export(HttpServletResponse response);

    //导入数据
    void importData(MultipartFile multipartFile);

    //根据parentDictCode,value获取名字
    String getName(String parentDictCode, String value);

    //根据dictCode获取下级数据
    List<Dict> findByDictcode(String dictCode);
}
