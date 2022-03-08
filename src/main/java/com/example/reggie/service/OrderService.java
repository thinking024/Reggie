package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.pojo.Orders;

public interface OrderService extends IService<Orders> {
    /**
     * 用户下单
     *
     * @param orders
     */
    void submit(Orders orders);

    void again(Long orderId);

    Page getByUserIdWithOrderDetails(int page, int pageSize);

    OrdersDto getByIdWithOrderDetails(Long id);
}