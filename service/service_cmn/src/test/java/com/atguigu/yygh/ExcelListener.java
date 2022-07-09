package com.atguigu.yygh;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-21 14:34
 */
public class ExcelListener extends AnalysisEventListener<Stu> {
    List<Stu> list = new ArrayList<Stu>();
    @Override
    public void invoke(Stu stu, AnalysisContext analysisContext) {
        System.out.println("表数据 = " + stu);
        list.add(stu);
    }


    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        System.out.println("表头 = " + headMap);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
