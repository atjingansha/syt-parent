package com.atguigu.yygh;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author WangJin
 * @create 2022-06-21 14:19
 */
@Data
public class Stu {

    //设置表头名称
    @ExcelProperty(value = "学生编号",index =0)
    private int sno;

    //设置表头名称
    @ExcelProperty(value = "学生姓名",index = 1)
    private String sname;

}
