package com.atguigu.yygh;

import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WangJin
 * @create 2022-06-21 14:20
 */
public class WriteTest {
    public static void main(String[] args) {
        String fileName = "D:\\11.xlsx";

        EasyExcel.write(fileName,Stu.class).sheet("学生信息").doWrite(data());



    }



    private static List<Stu> data() {
        List<Stu> list = new ArrayList<Stu>();
        for (int i = 0; i < 10; i++) {
            Stu data = new Stu();
            data.setSno(i);
            data.setSname("张三"+i);
            list.add(data);
        }
        return list;
    }
}
