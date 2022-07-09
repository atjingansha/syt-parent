package com.atguigu.yygh.order.mapper;

import com.atguigu.model.order.OrderInfo;
import com.atguigu.vo.order.OrderCountQueryVo;
import com.atguigu.vo.order.OrderCountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author WangJin
 * @create 2022-07-07 14:32
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {


    /**统计平台预约数*/
    List<OrderCountVo> selectOrderCount(OrderCountQueryVo orderCountQueryVo);
}
