package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.pojo.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    void removeBatchById(List<Long> ids);
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    boolean saveWithDish(SetmealDto setmealDto);

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    SetmealDto getByIdWithDish(Long id);

    //更新菜品信息，同时更新对应的口味信息
    boolean updateWithDish(SetmealDto setmealDto);

    // 根据id批量更改售卖状态
    boolean changeStatusBatchById(int status, List<Long> ids);
}
