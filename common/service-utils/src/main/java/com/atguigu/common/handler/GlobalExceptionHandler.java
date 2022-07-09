package com.atguigu.common.handler;

import com.atguigu.yygh.common.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author WangJin
 * @create 2022-06-16 15:57
 */
@ControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(Exception.class)
    @ResponseBody
    public R error(Exception e){
        e.printStackTrace();

        return R.error();
    }


    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public R arith(ArithmeticException e){
        e.printStackTrace();

        return R.error().message("特殊异常处理");
    }

    @ExceptionHandler(YughException.class)
    @ResponseBody
    public R Yugh(YughException e){
        e.printStackTrace();

        return R.error().code(e.getCode()).message(e.getMsg());
    }
}
