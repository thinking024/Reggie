package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.DishDto;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.pojo.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    void removeBatchById(List<Long> ids);

    //根据id查询菜品信息和对应的口味信息
    DishDto getByIdWithFlavor(Long id);

    boolean changeStatusBatchById(int status, List<Long> ids);

    boolean saveWithFlavor(DishDto dishDto);

    //更新菜品信息，同时更新对应的口味信息
    boolean updateWithFlavor(DishDto dishDto);
}
