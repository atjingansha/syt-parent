package com.atguigu.yygh.msm.service;

import com.atguigu.vo.msm.MsmVo;

import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-29 14:23
 */
public interface MsmService {

    /**发送短信*/
    boolean send(String phone, Map<String, Object> paramMap);

    /**发送短信接口*/
    boolean send(MsmVo msmVo);
}
