package com.atguigu.yygh;

import com.alibaba.excel.EasyExcel;

/**
 * @author WangJin
 * @create 2022-06-21 14:39
 */
public class ReadTest {

    public static void main(String[] args) {

        String fileName = "D:\\11.xlsx";

        EasyExcel.read(fileName, Stu.class, new ExcelListener()).sheet().doRead();
    }
}
