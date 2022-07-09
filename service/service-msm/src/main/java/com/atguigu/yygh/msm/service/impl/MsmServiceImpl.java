package com.atguigu.yygh.msm.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.common.handler.YughException;
import com.atguigu.vo.msm.MsmVo;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.utils.RandomUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-29 14:24
 */
@Service
public class MsmServiceImpl implements MsmService {


    @Override
    public boolean send(String phone, Map<String, Object> paramMap) {
        //1校验手机号
        if(StringUtils.isEmpty(phone)){return false;}
        //2创建客户端对象
        DefaultProfile profile =
                DefaultProfile.getProfile("default",
                        "LTAI5tNnjuL1kV8qL8AmFKjR",
                        "UVcFd8EFRKnBW5hNTU1E2ISwjYUssP");
        IAcsClient client = new DefaultAcsClient(profile);
        //3创建请求对象，设置参数
        CommonRequest request = new CommonRequest();
        //request.setProtocol(ProtocolType.HTTPS);
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");

        request.putQueryParameter("PhoneNumbers", phone);
        request.putQueryParameter("SignName", "我的谷粒在线教育网站");
        request.putQueryParameter("TemplateCode", "SMS_183195440");
        request.putQueryParameter("TemplateParam", JSONObject.toJSONString(paramMap));

        try {
            //4客户端对象调用方法，发送请求，获得响应
            CommonResponse response = client.getCommonResponse(request);
            //5从响应获取信息
            System.out.println(response.getData());
            return response.getHttpResponse().isSuccess();
        } catch (ClientException e) {
            e.printStackTrace();
            throw new YughException(20001,"发送短信失败");
        }
    }

    @Override
    public boolean send(MsmVo msmVo) {
        String phone=msmVo.getPhone();
        String templateCode = msmVo.getTemplateCode();
        Map<String, Object> param = msmVo.getParam();

        //模拟发送通知消息,发送验证码
        String code = RandomUtil.getFourBitRandom();
        Map<String,Object>  paramMap=new HashMap<>();

        paramMap.put("code",code);
        boolean send = this.send(phone, paramMap);
        return send;
    }

}
