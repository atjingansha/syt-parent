package com.atguigu.yygh.service.impl;

import com.atguigu.common.handler.YughException;
import com.atguigu.yygh.mapper.HospitalSetMapper;
import com.atguigu.model.hosp.HospitalSet;
import com.atguigu.yygh.service.HospitalSetService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author WangJin
 * @create 2022-06-15 9:34
 */
@Service
public class HospitalSetServiceImpl
        extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {


    @Override
    public String getSignKey(String hospcode) {

        QueryWrapper<HospitalSet> wrapper=new QueryWrapper<>();

        wrapper.eq("hoscode",hospcode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);

        if (hospitalSet!=null){
            return hospitalSet.getSignKey();
        }else {
            throw new YughException(20001,"获取签名失败");
        }

    }
}
