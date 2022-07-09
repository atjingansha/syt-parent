package com.atguigu.yygh.user.service;

import com.atguigu.model.user.UserInfo;
import com.atguigu.vo.user.LoginVo;
import com.atguigu.vo.user.UserAuthVo;
import com.atguigu.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-29 9:31
 */
public interface UserInfoService extends IService<UserInfo> {

    //用户登录接口
    Map<String, Object> login(LoginVo loginVo);

    /**查询用户信息*/
    UserInfo getUserInfo(Long userId);

    /**保存用户认证信息*/
    void userAuth(Long userId, UserAuthVo userAuthVo);

    //带条件分页查询用户列表
    Page<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    //用户锁定
    void lock(Long userId, Integer status);

    //用户详情
    Map<String, Object> show(Long userId);

    //认证审核
    void approval(Long userId, Integer authStatus);
}
