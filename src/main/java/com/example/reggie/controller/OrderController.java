package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import com.example.reggie.dto.OrdersDto;
import com.example.reggie.pojo.Employee;
import com.example.reggie.pojo.Orders;
import com.example.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 用户再次下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        orderService.again(orders.getId());
        return R.success("下单成功");
    }

    /**
     * 查询用户自己的订单，dto形式
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(int page, int pageSize) {
        log.info("page = {},pageSize = {}", page, pageSize);
        Page pages = orderService.getByUserIdWithOrderDetails(page, pageSize);
        return R.success(pages);
    }

    @GetMapping("/orderDetail/{number}")
    public R<OrdersDto> userPage(@PathVariable String number) {
        OrdersDto dto = orderService.getByIdWithOrderDetails(Long.valueOf(number));
        return R.success(dto);
    }

    /**
     * 管理后台按条件查询订单
     *
     * @param page
     * @param pageSize
     * @param number    订单id，非必要
     * @param beginTime 起始时间，非必要
     * @param endTime   结束时间，非必要
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, Long number,
                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
                        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        log.info("page = {},pageSize = {}", page, pageSize);
        Page pageInfo = new Page(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(number != null, Orders::getId, number);
        queryWrapper.between(beginTime != null && endTime != null, Orders::getOrderTime, beginTime, endTime);
        queryWrapper.orderByAsc(Orders::getStatus);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping()
    public R changeStatus(@RequestBody Orders orders) {
        log.info("orders = {}", orders);
        if (orderService.updateById(orders)) {
            return R.success("订单修改成功");
        }
        return R.error("订单修改失败");
    }
}