package com.atguigu.yygh.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.atguigu.model.cmn.Dict;
import com.atguigu.vo.cmn.DictEeVo;
import com.atguigu.yygh.mapper.DictMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author WangJin
 * @create 2022-06-21 20:11
 */
@Component
public class DictListener extends AnalysisEventListener<DictEeVo>{

    @Autowired
    private DictMapper dictMapper;

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {

        Dict dict=new Dict();

        BeanUtils.copyProperties(dictEeVo,dict);

        dict.setIsDeleted(0);

        dictMapper.insert(dict);

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
