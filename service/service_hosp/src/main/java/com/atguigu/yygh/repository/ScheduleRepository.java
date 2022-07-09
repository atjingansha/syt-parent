package com.atguigu.yygh.repository;

import com.atguigu.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author WangJin
 * @create 2022-06-24 22:01
 */


@Repository
public interface ScheduleRepository extends MongoRepository<Schedule,String> {
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> getByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date toDate);
}

