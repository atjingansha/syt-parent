package com.atguigu.yygh.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.handler.YughException;
import com.atguigu.model.user.UserInfo;
import com.atguigu.yygh.common.R;
import com.atguigu.yygh.common.utils.JwtHelper;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantPropertiesUtil;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-07-01 10:35
 */
@Api("微信对接接口")
@Controller
@RequestMapping("/api/ucenter/wx")
public class WeiXinApiController {

    @Autowired
    private UserInfoService userInfoService;

    /***
     * 获取微信登录参数
     */
    @ResponseBody
    @GetMapping("getLoginParam")
    public R genQrConnect(HttpSession session) throws Exception{
        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String,Object> map=new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis()+"");//System.currentTimeMillis()+""
        return R.ok().data(map);
    }





    @GetMapping("callback")
    public String callback(String code, String state, HttpSession session) {
        //1、获取临时验证码信息参数
        System.out.println("code = " + code);
        System.out.println("state = " + state);
        //2、拿code换取access_token,open_id
        //2.1拼接访问请求（url地址+参数）
        //方式一
        //String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
        //        "appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";
        //方式二
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        try {
            //2.2发送请求，获得响应
            String accessTokenStr = HttpClientUtils.get(accessTokenUrl);
            System.out.println("accessTokenStr = " + accessTokenStr);
            //2.3取出参数
            JSONObject accessTokenJson = JSONObject.parseObject(accessTokenStr);
            String access_token = (String) accessTokenJson.get("access_token");
            String openid = (String) accessTokenJson.get("openid");

            //3、根据open_id查询用户信息
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("openid",openid);
            UserInfo userInfo = userInfoService.getOne(wrapper);

            //4、用户信息为空、走注册（调用微信获取用户信息）
            if(userInfo==null){
                //4.1拼写获取用户信息的url
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";
                String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
                //4.2发送请求获取响应
                String resultUserInfo = HttpClientUtils.get(userInfoUrl);
                System.out.println("resultUserInfo = " + resultUserInfo);
                //4.3转化对象,获取参数
                JSONObject userInfoJson = JSONObject.parseObject(resultUserInfo);
                //用户昵称
                String nickname = userInfoJson.getString("nickname");
                //用户头像
                String headimgurl = userInfoJson.getString("headimgurl");
                //4.4实现注册
                userInfo = new UserInfo();
                userInfo.setOpenid(openid);
                userInfo.setNickName(nickname);
                userInfo.setStatus(1);
                userInfoService.save(userInfo);
            }
            //5、用户信息不为空、判断用户是否锁定
            if(userInfo.getStatus()==0){
                throw  new YughException(20001,"用户已被锁定");
            }
            Map<String,Object> map = new HashMap<>();
            //6、验证是否绑定手机号
            if(StringUtils.isEmpty(userInfo.getPhone())){
                //如果没有绑定手机号，openid=微信唯一编号
                map.put("openid",openid);
            }else{
                //如果绑定手机号，openid=“”
                map.put("openid","");
            }
            //7、补全数据
            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            map.put("name",name);
            //8、走登录步骤
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token",token);
            //9、重定向返回页面
            return "redirect:http://localhost:3000/weixin/callback?token="
                    +map.get("token")+ "&openid="+map.get("openid")
                    +"&name="+URLEncoder.encode((String)map.get("name"),"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            throw new YughException(20001,"微信扫码登录失败");
        }

    }





}
