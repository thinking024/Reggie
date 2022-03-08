package com.example.reggie.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.pojo.OrderDetail;
import com.example.reggie.pojo.Orders;
import org.springframework.beans.BeanUtils;

import java.util.List;

public interface OrderDetailService extends IService<OrderDetail> {

     OrdersDto getByIdWithOrders(Long id);
}