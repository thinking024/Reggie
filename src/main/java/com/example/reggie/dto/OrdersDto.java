package com.example.reggie.dto;

import com.example.reggie.pojo.DishFlavor;
import com.example.reggie.pojo.OrderDetail;
import com.example.reggie.pojo.Orders;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrdersDto extends Orders {
    private List<OrderDetail> orderDetails = new ArrayList<>();

    private Integer copies;
}
