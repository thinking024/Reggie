package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.common.CustomException;
import com.example.reggie.dto.DishDto;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.mapper.SetMealMapper;
import com.example.reggie.pojo.Dish;
import com.example.reggie.pojo.DishFlavor;
import com.example.reggie.pojo.Setmeal;
import com.example.reggie.pojo.SetmealDish;
import com.example.reggie.service.SetmealDishService;
import com.example.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal> implements SetmealService {
    @Value("${reggie.path}")
    private String basePath;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    @Transactional
    public void removeBatchById(List<Long> ids) {
        //查询套餐状态，确定是否可用删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        this.removeByIds(ids);
    }

    /**
     * 根据id查询套餐信息和包含的菜品
     *
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //查询菜品基本信息，从dish表查询
        Setmeal setmeal = this.getById(id);

        SetmealDto dto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, dto);

        //查询当前套餐包含的的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());

        List<SetmealDish> dishes = setmealDishService.list(queryWrapper);
        dto.setSetmealDishes(dishes);
        return dto;
    }

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDto
     */
    @Transactional
    public boolean saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        if (!this.save(setmealDto))
            return false;
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息，操作setmeal_dish,执行insert操作
        return setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public boolean updateWithDish(SetmealDto setmealDto) {
//      todo 在这里将原来的图片清理掉，但是无法防止 前端反复上传新图片且最后取消，只能删掉一张
        Setmeal setmeal = this.getById(setmealDto);
        log.info("old image{}", setmeal.getImage());
        if (!setmeal.getImage().equals(setmealDto.getImage())) {
            FileSystemUtils.deleteRecursively(new File(basePath + setmeal.getImage()));
        }
        if (this.updateById(setmealDto) == false)
            return false;

        //清理当前套餐对应菜品数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //添加当前提交过来的菜品数据
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        dishes = dishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        return setmealDishService.saveBatch(dishes);
    }

    /**
     * 根据id批量更改售卖状态
     *
     * @param status 状态
     * @param ids    id列表
     * @return
     */
    @Override
    public boolean changeStatusBatchById(int status, List<Long> ids) {
        if (ids == null || ids.size() == 0)
            return false;
        ArrayList<Setmeal> list = new ArrayList<>();
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            list.add(setmeal);
        }
        return this.updateBatchById(list);
    }
}
