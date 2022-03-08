package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.mapper.OrderDetailMapper;
import com.example.reggie.pojo.OrderDetail;
import com.example.reggie.pojo.Orders;
import com.example.reggie.service.OrderDetailService;
import com.example.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

    @Autowired
    private OrderService orderService;

    @Override
    public OrdersDto getByIdWithOrders(Long id) {

        // 先查询订单
        LambdaQueryWrapper<Orders> orderWrapper = new LambdaQueryWrapper();
        orderWrapper.eq(Orders::getId, id);
        Orders one = orderService.getOne(orderWrapper);

        // 再查订单详情
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> details = this.list(detailWrapper);

        // 整合为dto
        OrdersDto dto = new OrdersDto();
        BeanUtils.copyProperties(one, dto);
        dto.setOrderDetails(details);
        return dto;
    }
}