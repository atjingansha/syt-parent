package com.atguigu.yygh.service;

import com.atguigu.model.hosp.Schedule;
import com.atguigu.vo.hosp.ScheduleOrderVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author WangJin
 * @create 2022-06-24 22:00
 */
public interface ScheduleService {

    /**获取排班信息带分页*/
    Page<Schedule> getSchedule(int page, int limit, ScheduleOrderVo scheduleOrderVo);


    /**增加排班信息*/
    void saveSchedule(Map<String, Object> paramMap);

    /**删除排班信息*/
    void delSchedule(String hoscode, String hosScheduleId);

    /**排班日期统计*/
    Map<String, Object> getRuleSchedule(long page, long limit, String hoscode, String depcode);

    /**查询排班详细信息*/
    List<Schedule> getScheduleDetails(String hoscode, String depcode, String workDate);

    /**获取可预约排班数据*/
    Map<String, Object> getBookigSchedule(Integer page, Integer limit, String hoscode, String depcode);


    /**根据排班id获取排班详情*/
    Schedule findScheduleById(String id);

    /**根据排班id获取预约下单数据*/
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    /**修改排班*/
    void update(Schedule schedule);

    /**查询排班信息*/
    Schedule getScheduleByInfo(String hoscode, String scheduleId);
}
