package com.atguigu.yygh.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.common.handler.YughException;
import com.atguigu.model.cmn.Dict;
import com.atguigu.vo.cmn.DictEeVo;
import com.atguigu.yygh.listener.DictListener;
import com.atguigu.yygh.mapper.DictMapper;
import com.atguigu.yygh.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author WangJin
 * @create 2022-06-21 9:31
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper,Dict>  implements DictService{

    @Autowired
    private DictListener dictListener;

    /**根据id查询子节点**/
    @Override
    @Cacheable(value = "dict",key = "'selectDictlist'+#id")
    public List<Dict> findChlidData(Long id) {

        QueryWrapper<Dict>  wrapper=new QueryWrapper<>();
        wrapper.eq("parent_id",id);

        List<Dict> dictList = baseMapper.selectList(wrapper);

        for (Dict dict : dictList) {
            Long dictId = dict.getId();
            boolean childData = isChildData(dictId);
            dict.setHasChildren(childData);
        }

        return dictList;
    }
/**查询是否有子节点*/
    public boolean isChildData(Long id){
        QueryWrapper<Dict> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("parent_id",id);

        Integer count = baseMapper.selectCount(queryWrapper);

        return count>0;
    }

      /**导出数据*/
    @Override
    public void export(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");

            List<Dict> dictList = baseMapper.selectList(null);


            List<DictEeVo> dictEeVoList=new ArrayList<>();
            for (Dict dict : dictList) {
                DictEeVo dictEeVo=new DictEeVo();

                BeanUtils.copyProperties(dict,dictEeVo);

                dictEeVoList.add(dictEeVo);
            }

            EasyExcel.write(response.getOutputStream(),DictEeVo.class).sheet("数据字典").doWrite(dictEeVoList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new YughException(2011,"读取失败");
        }
    }

    @Override
    public void importData(MultipartFile multipartFile) {

        try {
            InputStream inputStream = multipartFile.getInputStream();

            EasyExcel.read(inputStream,DictEeVo.class,dictListener).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
            throw new YughException(20001,"导入失败");
        }
    }

    @Override
    public String getName(String parentDictCode, String value) {

        if (StringUtils.isEmpty(parentDictCode)){
            //国标
          Dict dict=  baseMapper.selectOne(new QueryWrapper<Dict>().eq("value",value));

          if (dict!=null){
              return dict.getName();
          }
        }else {
            //自定义
            QueryWrapper<Dict> wrapper=new QueryWrapper<>();
            wrapper.eq("dict_code",parentDictCode);

            Dict dictByDictCode=baseMapper.selectOne(wrapper);


            QueryWrapper<Dict> queryWrapper=new QueryWrapper<>();

            System.out.println(dictByDictCode.getParentId());
            queryWrapper.eq("parent_id",dictByDictCode.getId())
                            .eq("value",value);

            Dict dict = baseMapper.selectOne(queryWrapper);

            if (dict!=null){
                return dict.getName();
            }
        }
        return null;
    }

    @Override
    public List<Dict> findByDictcode(String dictCode) {

        QueryWrapper<Dict> wrapper=new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);

        Dict dictByDictCode=baseMapper.selectOne(wrapper);

        QueryWrapper<Dict> queryWrapper=new QueryWrapper<>();

       queryWrapper.eq("parent_id", dictByDictCode.getId());
        List<Dict> list = baseMapper.selectList(queryWrapper);

        return list;
    }
}
