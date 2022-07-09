package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.handler.YughException;
import com.atguigu.enums.OrderStatusEnum;
import com.atguigu.model.order.OrderInfo;
import com.atguigu.model.user.Patient;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import com.atguigu.vo.msm.MsmVo;
import com.atguigu.vo.order.OrderCountQueryVo;
import com.atguigu.vo.order.OrderCountVo;
import com.atguigu.vo.order.OrderMqVo;
import com.atguigu.vo.order.OrderQueryVo;
import com.atguigu.yygh.common.service.MqConst;
import com.atguigu.yygh.common.service.RabbitService;
import com.atguigu.yygh.hosp.HospitalFeignClient;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.WeixinService;
import com.atguigu.yygh.order.utils.HttpRequestHelper;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author WangJin
 * @create 2022-07-07 14:33
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Autowired
    private PatientFeignClient patientFeignClient;

    @Autowired
    private HospitalFeignClient hospitalFeignClient;

    @Autowired
    private RabbitService rabbitService;

    @Autowired
    private WeixinService weixinService;
    /**创建订单*/
    @Override
    public Long submitOrder(String scheduleId, Long patientId) {

        //1.根据patientId跨模块user查询就诊人信息
        Patient patient = patientFeignClient.getPatient(patientId);
        if (patient==null){
            throw new YughException(20001,"就诊人信息有误");
        }
        //2.根据scheduleId跨模块hosp查询排班信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        if (scheduleOrderVo==null){
            throw new YughException(20001,"排班信息有误");
        }

        //判断当前时间是否可以预约
        if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
        || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()){
            throw new YughException(20001,"当前不在挂号时间");
        }
        //判断当前是否有号
        if (scheduleOrderVo.getAvailableNumber()<=0){
            throw new YughException(20001,"当前已无号");
        }

        //3.整合信息,生成订单
        OrderInfo orderInfo=new OrderInfo();
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo);
        String outTradeNo = System.currentTimeMillis() + ""+ new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        this.save(orderInfo);

        //4.调用医院接口,确认挂号成功后更新订单信息
        Map<String,Object> paramMap=new HashMap<>();

        //封装参数
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getHosScheduleId());
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        //String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", "");

        //调用接口
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");

        if (result.getInteger("code")==200){
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");;
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");;
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);
            //排班可预约数
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //5. 发送MQ消息,更新号源信息,通知就诊人
            OrderMqVo orderMqVo=new OrderMqVo();
            orderMqVo.setHoscode(orderInfo.getHoscode());
            orderMqVo.setScheduleId(orderInfo.getHosScheduleId());
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setAvailableNumber(availableNumber);


            //短信提示
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("amount", orderInfo.getAmount());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            msmVo.setParam(param);
            orderMqVo.setMsmVo(msmVo);

            //发送短信消息
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);

        }else {
            throw new YughException(20001,"挂号失败");
        }

        return orderInfo.getId();
    }

    /**
     * 带条件带分页查询订单
     * @param pageParam
     * @param orderQueryVo
     * @return
     */
    @Override
    public Page<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {

        Long userId = orderQueryVo.getUserId();
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();

        QueryWrapper<OrderInfo> wrapper=new QueryWrapper<>();

        if (!StringUtils.isEmpty(userId)){
            wrapper.eq("user_id",userId);
        }

        if (!StringUtils.isEmpty(name)){
            wrapper.eq("hosname",name);
        }
        if(!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id",patientId);
        }
        if(!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status",orderStatus);
        }
        if(!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date",reserveDate);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }


        Page<OrderInfo> orderInfoPage = baseMapper.selectPage(pageParam, wrapper);


        orderInfoPage.getRecords().stream().forEach(item->{
            this.packOrderInfo(item);
        });

        return orderInfoPage;
    }

    /**根据订单id查询订单详情*/
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = this.packOrderInfo(baseMapper.selectById(orderId));

        return orderInfo;
    }

    /**取消预约*/
    @Override
    public Boolean cancelOrder(Long orderId) {
        //1.根据orderId查询订单
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        if (orderInfo==null){
            throw new YughException(20001,"订单消息有误");
        }
        //2.判断是否已过退号时间
        DateTime quitDateTime=new DateTime(orderInfo.getQuitTime());

        if (quitDateTime.isBeforeNow()){
            throw new YughException(20001,"已过退号时间,请到医院办理");
        }
        //3.调用医院接口退号
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        reqMap.put("sign", "");

        JSONObject result = HttpRequestHelper.sendRequest(reqMap, "http://localhost:9998/order/updateCancelStatus");

        if (result.getInteger("code")!=200){
            throw new YughException(20001,"取消预约失败");
        }else {
            //4.如果医院取消成功查询是否已支付
            if (orderInfo.getOrderStatus().intValue()==
            OrderStatusEnum.PAID.getStatus()){
                //5.如果已支付,退钱
                Boolean refund = weixinService.refund(orderId);
                if (!refund){
                    throw new YughException(20001,"微信退款失败");
                }
            }

            //6.更新订单状态
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            baseMapper.updateById(orderInfo);
            //7.发送MQ消息,更新号源,发送通知

        }

        return true;
    }


    /**就诊提醒*/
    @Override
    public void patientTips() {
        QueryWrapper<OrderInfo> wrapper=new QueryWrapper<>();
        wrapper.eq("reserve_date",new DateTime().toString("yyyy-MM-dd"));
        List<OrderInfo> orderInfoList = baseMapper.selectList(wrapper);

        for (OrderInfo orderInfo : orderInfoList) {
            //短信提示
            MsmVo msmVo=new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());

            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");

            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            msmVo.setParam(param);

            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM,msmVo);
        }
    }


    /**获取订单统计数据*/
    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {


        List<OrderCountVo> orderCountVoList = baseMapper.selectOrderCount(orderCountQueryVo);

        //取出x轴,y轴数据
        List<String> dateList=orderCountVoList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        List<Integer> countList=orderCountVoList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());

        Map<String, Object> map=new HashMap<>();
        map.put("dateList",dateList);
        map.put("countList",countList);

        return map;
    }

    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }
}
