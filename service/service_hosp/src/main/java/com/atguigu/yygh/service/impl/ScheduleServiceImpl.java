package com.atguigu.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.handler.YughException;
import com.atguigu.model.hosp.BookingRule;
import com.atguigu.model.hosp.Department;
import com.atguigu.model.hosp.Hospital;
import com.atguigu.model.hosp.Schedule;
import com.atguigu.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.repository.ScheduleRepository;
import com.atguigu.yygh.service.DepartmentService;
import com.atguigu.yygh.service.HospitalService;
import com.atguigu.yygh.service.ScheduleService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author WangJin
 * @create 2022-06-24 22:00
 */
@Service
public class ScheduleServiceImpl implements ScheduleService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Override
    public Page<Schedule> getSchedule(int page, int limit, ScheduleOrderVo scheduleOrderVo) {

        Schedule schedule=new Schedule();

        BeanUtils.copyProperties(scheduleOrderVo,schedule);

        ExampleMatcher matcher=ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Example example=Example.of(schedule,matcher);

        Sort sort=Sort.by(Sort.Direction.DESC,"createTime");

        Pageable pageable= PageRequest.of(page,limit,sort);

        Page<Schedule> pageModel = scheduleRepository.findAll(example, pageable);

        return pageModel;

    }

    @Override
    public void saveSchedule(Map<String, Object> paramMap) {
        String jsonString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);

        Schedule   targetSchedule=  scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

        if (targetSchedule!=null){
            schedule.setCreateTime(schedule.getCreateTime());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(schedule.getIsDeleted());
            schedule.setId(schedule.getId());
            scheduleRepository.save(schedule);
        }else {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }
    }

    @Override
    public void delSchedule(String hoscode, String hosScheduleId) {

        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);

        if(schedule==null){
            throw new YughException(20001,"没有这条数据");
        }else {
            scheduleRepository.deleteById(schedule.getId());
        }


    }

    @Override
    public Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode) {
        //1.定义最终返回对象
        Map<String, Object> result=new HashMap<>();

        System.out.println(depcode);
        System.out.println(hoscode);
        //2.带条件带分页的聚合查询(list)
        //2.1创建查询条件对象
        Criteria criteria=Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode);
        //2.2创建聚合查询对象
        Aggregation agg=Aggregation.newAggregation(
                //筛选条件
                Aggregation.match(criteria),
                //分组聚合信息设置
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.ASC,"workDate"),
                //分页
                Aggregation.skip((page-1)*limit),
                //每页记录数
                Aggregation.limit(limit)
        );


        //2.3聚合查询
        AggregationResults<BookingScheduleRuleVo> aggregate =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = aggregate.getMappedResults();


        //3.带条件带分页的聚合查询(total)

        Aggregation aggTotal=Aggregation.newAggregation(
                //筛选条件
                Aggregation.match(criteria),
                //分组聚合信息设置
                Aggregation.group("workDate")

        );
        AggregationResults<BookingScheduleRuleVo> aggregateTotal = mongoTemplate.aggregate(aggTotal, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> totalList = aggregateTotal.getMappedResults();

        int total=totalList.size();

        //4.日期转化成周几
        for (BookingScheduleRuleVo bookingScheduleRuleVo : bookingScheduleRuleVoList) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            DateTime dateTime = new DateTime(workDate);
            String dayOfWeek = this.getDayOfWeek(dateTime);
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //5.封装数据
        result.put("bookingScheduleRuleList",bookingScheduleRuleVoList);
        result.put("total",total);

        //6.补全信息
        //获取医院名称
        String hosName = hospitalService.getHospName(hoscode);
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname",hosName);
        result.put("baseMap",baseMap);

        return result;
    }

    @Override
    public List<Schedule> getScheduleDetails(String hoscode, String depcode, String workDate) {
        List<Schedule>  list= scheduleRepository.getByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());


        list.stream().forEach(item->{
            this.packageSchedule(item);
        });

        return list;
    }


    @Override
    public Map<String, Object> getBookigSchedule(Integer page, Integer limit, String hoscode, String depcode) {

        //1.根据hoscode查询预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital==null){
            throw new YughException(20001,"医院信息有误");
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //2.根据预约规则,分页参数查询可以预约的日期集合(IPage<Date>)
        IPage<Date> iPage=this.getDateListPage(page,limit,bookingRule);
        List<Date> dateList = iPage.getRecords();


        //3.参考后台接口,实现聚合查询排班信息
        //3.1创建查询条件对象
        Criteria criteria=Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode)
                .and("workDate").in(dateList);

        Aggregation agg=Aggregation.newAggregation(
                //筛选条件
                Aggregation.match(criteria),
                //分组聚合信息设置
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber")
                );

        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> scheduleVoList = aggregate.getMappedResults();

        //类型转化list->map
        Map<Date,BookingScheduleRuleVo> scheduleVoMap=new HashMap<>();

        if (!CollectionUtils.isEmpty(scheduleVoList)){
            scheduleVoMap = scheduleVoList.stream().collect(Collectors.toMap(
                    BookingScheduleRuleVo::getWorkDate,
                    BookingScheduleRuleVo->BookingScheduleRuleVo));
        }
        //4.将步骤2,3进行整合

        //4.1创建合并后的数据集合
       List<BookingScheduleRuleVo> bookingScheduleRuleVoList=new ArrayList<>();
        //4.2遍历dateList取出每一天date
        for (int i = 0,let=dateList.size(); i < let; i++) {
            Date date = dateList.get(i);
            //4.3从scheduleVoMap取出对应排班统计数据
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            //4.4如果对应排班统计数据为空,初始化统计数据
            if (bookingScheduleRuleVo==null){
                bookingScheduleRuleVo=new BookingScheduleRuleVo();
                bookingScheduleRuleVo.setDocCount(0);
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            //4.5设置排班日期
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //4.6根据date换算周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
            //4.7根据时间判断统计数据状态值,0正常,1即将放号,-1当天已停止挂号
            //最后一页,最后一条状态1,即将放号
            if (i==let-1&&page==iPage.getPages()){
                bookingScheduleRuleVo.setStatus(1);
            }else {
                bookingScheduleRuleVo.setStatus(0);
            }

            //第一页第一条判断是否过停止挂号时间(日期+时间)
            if (i==0&&page==1){
                DateTime stopDateTime=this.getDateTime(new Date(), bookingRule.getStopTime());
                if (stopDateTime.isBeforeNow()){
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }

            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }


        //5.封装数据返回
        Map<String, Object> result=new HashMap<>();

        //可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;

    }

    /**根据排班id获取排班详情*/
    @Override
    public Schedule findScheduleById(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();


        return this.packageSchedule(schedule);
    }



    /**根据排班id获取预约下单数据*/
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        //1.根据scheduleId查询排班信息
        Schedule schedule = scheduleRepository.findById(scheduleId).get();

        if(schedule==null){
            throw new YughException(20001,"排班信息有误");
        }
        //2.根据排班信息hoscode,查询医院信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if(hospital==null){
            throw new YughException(20001,"医院信息有误");
        }
        //3.获取规则信息
        BookingRule bookingRule = hospital.getBookingRule();
        if(bookingRule==null){
            throw new YughException(20001,"规则信息有误");
        }
        //4.封装基础数据
        ScheduleOrderVo scheduleOrderVo=new ScheduleOrderVo();
        scheduleOrderVo.setHoscode(hospital.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode()).getDepname());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //5.封装根据规则计算出的时间数据
        //退号截止天
        DateTime quitDate=new DateTime(schedule.getWorkDate()).plusDays(bookingRule.getQuitDay());

        DateTime quitDateTime=this.getDateTime(quitDate.toDate(),bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitDateTime.toDate());

        //预约开始天
        DateTime startTime=this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约结束时间
        DateTime endTime=this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime=this.getDateTime(new Date(),bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());
        return scheduleOrderVo;
    }

    /**修改排班*/
    @Override
    public void update(Schedule schedule) {
        scheduleRepository.save(schedule);
    }

    @Override
    public Schedule getScheduleByInfo(String hoscode, String scheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, scheduleId);
        return schedule;
    }

    /**根据预约规则,分页参数查询可以预约的日期集合**/
    private IPage<Date> getDateListPage(
            Integer page, Integer limit, BookingRule bookingRule) {

        //1.获取开始挂号时间(当前系统日期+开始挂号时间)
        DateTime releaseDateTime=this.getDateTime(new Date(), bookingRule.getReleaseTime());

        //2.获取预约周期,判断是否过了当天放号时间,如果过了时间周期+1
        Integer cycle = bookingRule.getCycle();
        if (releaseDateTime.isBeforeNow()){
            cycle +=1;
        }
        //3.根据周期推算可以挂号日期集合
        List<Date> dateList=new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime plusDays = new DateTime().plusDays(i);
            String plusDaysStr = plusDays.toString("yyyy-MM-dd");
            dateList.add(new DateTime(plusDaysStr).toDate());
        }
        //4.准备分页参数
        int start=(page-1)*limit;
        int end=(page-1)*limit+limit;

        if(end>dateList.size()){
            end= dateList.size();
        }
        //5.获取分页日期集合
        List<Date> datePageList=new ArrayList<>();
        for (int i = start; i < end; i++) {
            datePageList.add(dateList.get(i));
        }
        //6.返回IPage进行封装
        IPage<Date> iPage=new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page,limit, dateList.size());

        iPage.setRecords(datePageList);
        return iPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }


    /**
     * 根据日期获取周几数据
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }


    public Schedule packageSchedule(Schedule schedule){
        //设置医院名称
        schedule.getParam().put("hosname",hospitalService.getHospName(schedule.getHoscode()));
        //设置科室名称
        schedule.getParam().put("depname",departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
        //设置日期对应星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));

        return schedule;
    }

}
