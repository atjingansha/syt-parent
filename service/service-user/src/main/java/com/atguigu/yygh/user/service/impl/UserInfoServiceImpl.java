package com.atguigu.yygh.user.service.impl;

import com.atguigu.common.handler.YughException;
import com.atguigu.enums.AuthStatusEnum;
import com.atguigu.model.user.Patient;
import com.atguigu.model.user.UserInfo;
import com.atguigu.vo.user.LoginVo;
import com.atguigu.vo.user.UserAuthVo;
import com.atguigu.vo.user.UserInfoQueryVo;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-29 9:31
 */

@Service
public class UserInfoServiceImpl extends
        ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private PatientService patientService;

    //会员登录
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //1取出参数、验空
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        String openid = loginVo.getOpenid();
        if(StringUtils.isEmpty(phone)||StringUtils.isEmpty(code)){
            throw  new YughException(20001,"登录信息有误");
        }
        //2 校验验证码
        //2.1从redis获取验证码
        String redisCode = redisTemplate.opsForValue().get(phone);
        //2.2校验验证码
        if(!code.equals(redisCode)){
            throw  new YughException(20001,"验证码有误");
        }
        Map<String, Object> map = new HashMap<>();
        //2.5 验证openid,确认是手机号验证码登录、绑定手机号
        if(StringUtils.isEmpty(openid)){
            //3根据手机号查询用户信息
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone",phone);
            UserInfo userInfo = baseMapper.selectOne(wrapper);
            //4用户为空实现注册流程
            if(userInfo==null){
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                userInfo.setName("");
                userInfo.setStatus(1);
                baseMapper.insert(userInfo);
            }
            //5校验用户是否被锁定
            if(userInfo.getStatus()==0){
                throw  new YughException(20001,"用户已被锁定");
            }
            //6补全信息

            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }

            //7登录步骤
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("name",name);
            map.put("token",token);
        }else{
            //8绑定手机号
            //8.1根据openid查询用户信息
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("openid",openid);
            UserInfo userInfo = baseMapper.selectOne(wrapper);
            if(userInfo==null){
                throw  new YughException(20001,"用户注册信息有误");
            }
            //8.2更新用户手机号
            userInfo.setPhone(phone);
            baseMapper.updateById(userInfo);
            //5校验用户是否被锁定
            if(userInfo.getStatus()==0){
                throw  new YughException(20001,"用户已被锁定");
            }
            //6补全信息

            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }

            //7登录步骤
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("name",name);
            map.put("token",token);

        }
        return map;
    }

    @Override
    public UserInfo getUserInfo(Long userId) {
        UserInfo userInfo = this.packUserInfo(baseMapper.selectById(userId)) ;
        return userInfo;
    }



    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {

        UserInfo userInfo = baseMapper.selectById(userId);

        if(userInfo==null){
            throw new YughException(20001,"用户信息有误");
        }

        BeanUtils.copyProperties(userAuthVo,userInfo);

        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());

        baseMapper.updateById(userInfo);
    }

    @Override
    public Page<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        String name = userInfoQueryVo.getKeyword();//用户姓名
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间

        QueryWrapper<UserInfo> wrapper=new QueryWrapper<>();

        if (!StringUtils.isEmpty(name)){
            wrapper.like("name",name);
        }

        if (!StringUtils.isEmpty(status)){
            wrapper.eq("status",status);
        }

        if (!StringUtils.isEmpty(authStatus)){
            wrapper.eq("auth_status",authStatus);
        }

        if (!StringUtils.isEmpty(createTimeBegin)){
            wrapper.ge("create_time",createTimeBegin);
        }

        if (!StringUtils.isEmpty(createTimeEnd)){
            wrapper.le("create_time",createTimeEnd);
        }
        Page<UserInfo> pageModel = baseMapper.selectPage(pageParam, wrapper);

        pageModel.getRecords().stream().forEach(item->{
            this.packUserInfo(item);
        });


        return pageModel;
    }

    @Override
    public void lock(Long userId, Integer status) {
        if (status.intValue()==0||status.intValue()==1){
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> show(Long userId) {
        UserInfo userInfo = this.packUserInfo(baseMapper.selectById(userId));

        List<Patient> patientList = patientService.findAll(userId);

        Map<String,Object> map=new HashMap<>();

        map.put("userInfo",userInfo);
        map.put("patientList",patientList);

        return map;
    }


    //认证审核
    @Override
    public void approval(Long userId, Integer authStatus) {
        if (authStatus.intValue()==2||authStatus.intValue()==-1){
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }

    //翻译用户信息
    private UserInfo packUserInfo(UserInfo userInfo) {

        //认证状态
        String authStatusString =
                AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus());
        userInfo.getParam().put("authStatusString",authStatusString);

        //用户认证状态
        String statusString = userInfo.getStatus().intValue()==0 ?"锁定" : "正常";
        userInfo.getParam().put("statusString",statusString);
        return userInfo;
    }
}

