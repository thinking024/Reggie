package com.example.reggie.controller;

import com.example.reggie.common.R;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.service.OrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单明细
 */
@Slf4j
@RestController
@RequestMapping("/orderDetail")
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;

    @GetMapping("{number}")
    public R<OrdersDto> userPage(@PathVariable String number) {
        OrdersDto dto = orderDetailService.getByIdWithOrders(Long.valueOf(number));
        return R.success(dto);
    }
}