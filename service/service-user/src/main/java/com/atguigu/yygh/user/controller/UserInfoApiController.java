package com.atguigu.yygh.user.controller;

/**
 * @author WangJin
 * @create 2022-06-29 9:33
 */

import com.atguigu.model.user.UserInfo;
import com.atguigu.vo.user.LoginVo;
import com.atguigu.vo.user.UserAuthVo;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.IpUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Api("用户模块")
public class UserInfoApiController {

    @Autowired

    private UserInfoService userInfoService;


    @ApiOperation(value = "会员登录")
    @PostMapping("login")
    public R login(@RequestBody LoginVo loginVo, HttpServletRequest request) {

        loginVo.setIp(IpUtils.getIpAddr(request));
        Map<String, Object> info = userInfoService.login(loginVo);
        return R.ok().data(info);
    }



    @ApiOperation("用户认证接口")
    @PostMapping("auth/userAuth")
    public R userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        //获取用户id
        Long userId = AuthContextHolder.getUserId(request);
        //保存用户认证信息
        userInfoService.userAuth(userId,userAuthVo);

        return R.ok();
    }


    @ApiOperation("获取用户id信息接口")
    @GetMapping("auth/getUserInfo")
    public R getUserInfo(HttpServletRequest request) {

        Long userId = AuthContextHolder.getUserId(request);

        //根据id获取用户信息
        UserInfo userInfo=userInfoService.getUserInfo(userId);

        return R.ok().data("userInfo",userInfo);
    }
}