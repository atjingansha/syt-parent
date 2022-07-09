package com.atguigu.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.handler.YughException;
import com.atguigu.enums.DictEnum;
import com.atguigu.model.hosp.BookingRule;
import com.atguigu.model.hosp.Hospital;
import com.atguigu.vo.hosp.HospitalQueryVo;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.repository.HospitalRepository;
import com.atguigu.yygh.service.HospitalService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author WangJin
 * @create 2022-06-24 10:24
 */
@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
  private   HospitalRepository hospitalRepository;

@Autowired
private DictFeignClient dictFeignClient;

    @Override
    public void save(Map<String, Object> paramMap) {

        //1.参数对象转型Map->hosp

        String jsonString = JSONObject.toJSONString(paramMap);

        Hospital hospital = JSONObject.parseObject(jsonString, Hospital.class);

        //2.查询
      Hospital targetHospital= hospitalRepository.getByHoscode(hospital.getHoscode());

      if (targetHospital!=null){
          hospital.setId(targetHospital.getId());
          hospital.setCreateTime(targetHospital.getCreateTime());
          hospital.setUpdateTime(new Date());
          hospital.setStatus(targetHospital.getStatus());
          hospital.setIsDeleted(targetHospital.getIsDeleted());
          hospitalRepository.save(hospital);
      }else {
          hospital.setCreateTime(new Date());
          hospital.setUpdateTime(new Date());
          hospital.setStatus(0);
          hospital.setIsDeleted(0);
          hospitalRepository.save(hospital);
      }
    }

    @Override
    public Hospital getByHoscode(String hoscode) {

        Hospital hospital = hospitalRepository.getByHoscode(hoscode);

        return hospital;
    }

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {


        Hospital hospital=new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        ExampleMatcher matcher=ExampleMatcher.matching()
                .withIgnoreCase(true)
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Hospital> example=Example.of(hospital,matcher);

        Sort sort=Sort.by(Sort.Direction.DESC,"createTime");

        Pageable pageable= PageRequest.of((page-1),limit,sort);

        Page<Hospital> pageModel = hospitalRepository.findAll(example, pageable);


        // 跨模块使用数据字典翻译字段
        pageModel.getContent().stream().forEach(item ->{
            this.packHospital(item);
        });

        return pageModel;
    }



    private Hospital packHospital(Hospital hospital) {
        //翻译省市区(国标)
        String provinceString = dictFeignClient.getName(hospital.getProvinceCode());
        String cityString = dictFeignClient.getName(hospital.getCityCode());
        String districtString = dictFeignClient.getName(hospital.getDistrictCode());


        System.out.println(hospital.getHostype());
        System.out.println(DictEnum.HOSTYPE.getDictCode());
        //自定义
        String hostypeString = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(),hospital.getHostype());
        hospital.getParam().put("hostypeString", hostypeString);
        hospital.getParam().put("fullAddress", provinceString + cityString + districtString + hospital.getAddress());
        return hospital;
    }


    @Override
    public void updateStatus(String id, Integer status) {

        if (!(status.intValue()==0 || status.intValue()==1)){
            return;
        }

        Hospital hospital = hospitalRepository.findById(id).get();
        hospital.setStatus(status);
        hospital.setUpdateTime(new Date());
        hospitalRepository.save(hospital);

    }

    @Override
    public Map<String, Object> show(String id) {
        //根据id查询医院信息
        Hospital hospital = this.packHospital(hospitalRepository.findById(id).get());
        //从医院信息取出预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        hospital.setBookingRule(null);

        Map<String,Object> map=new HashMap<>();

        map.put("hospital",hospital);
        map.put("bookingRule",bookingRule);
        return map;
    }

    @Override
    public String getHospName(String hoscode) {
        Hospital hospital = hospitalRepository.getByHoscode(hoscode);

        if (hospital==null){
            throw new YughException(200001,"医院信息有误");
        }else {
            return hospital.getHosname();
        }
    }

    @Override
    public List<Hospital> findByHosnameLike(String hosname) {

        List<Hospital> list=hospitalRepository.getByHosnameLike(hosname);
        return list;
    }

    @Override
    public Map<String, Object> getHospInfoByHoscode(String hoscode) {
        //1.查询医院信息(翻译字段)
        Hospital hospital = this.packHospital(hospitalRepository.getByHoscode(hoscode));


        //2.取出预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        hospital.setBookingRule(null);
        //3.封装数据返回
        Map<String,Object> map=new HashMap<>();

        map.put("hospital", hospital);
        //预约规则
        map.put("bookingRule",bookingRule);
        return map;
    }

}
