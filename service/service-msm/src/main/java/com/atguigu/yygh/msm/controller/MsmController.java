package com.atguigu.yygh.msm.controller;

import com.atguigu.yygh.common.R;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.utils.RandomUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author WangJin
 * @create 2022-06-29 14:24
 */
@RestController
@RequestMapping("/api/msm")
@Api(description = "短信发送")
public class MsmController {

    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @GetMapping("/send/{phone}")
    @ApiOperation(value = "发送短信验证码")
    public R code(@PathVariable String phone){
        //1.从redis取出验证码,验证当前手机是否发送过验证码
        String redisCode = redisTemplate.opsForValue().get(phone);

        if (!StringUtils.isEmpty(redisCode)){
            return R.ok();
        }

        //2.生成验证码,封装参数
        String code = RandomUtil.getFourBitRandom();
        Map<String,Object> paramMap=new HashMap<>();

        paramMap.put("code",code);
        //3.调用方法发送短信

        boolean isSend=msmService.send(phone,paramMap);


        //4.发送成功后验证码存入redis,设置失效时间5分钟
        if (isSend){

            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.ok();
        }else {
            return R.error().message("发送短信失败");
        }
    }
}
