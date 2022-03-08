package com.example.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.reggie.common.CustomException;
import com.example.reggie.dto.DishDto;
import com.example.reggie.mapper.DishMapper;
import com.example.reggie.pojo.Dish;
import com.example.reggie.pojo.DishFlavor;
import com.example.reggie.pojo.Setmeal;
import com.example.reggie.pojo.SetmealDish;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
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
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Value("${reggie.path}")
    private String basePath;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @Transactional
    public boolean saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        if (!this.save(dishDto)) {
            return false;
        }
        Long dishId = dishDto.getId();//菜品id
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors != null && flavors.size() != 0) {
            flavors = flavors.stream().map((item) -> {
                item.setDishId(dishId);
                return item;
            }).collect(Collectors.toList());
            //保存菜品口味数据到菜品口味表dish_flavor
            return dishFlavorService.saveBatch(flavors);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean updateWithFlavor(DishDto dishDto) {
        Dish dish = this.getById(dishDto);
        log.info("old image{}", dish.getImage());
        if (!dish.getImage().equals(dishDto.getImage())) {
            FileSystemUtils.deleteRecursively(new File(basePath + dish.getImage()));
        }
        //更新dish表基本信息
        if (this.updateById(dishDto) == false)
            return false;

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        if (flavors != null && flavors.size() != 0) {
            flavors = flavors.stream().map((item) -> {
                item.setDishId(dishDto.getId());
                return item;
            }).collect(Collectors.toList());
            //保存菜品口味数据到菜品口味表dish_flavor
            return dishFlavorService.saveBatch(flavors);
        }
        return true;
    }

    /**
     * 根据id批量更改售卖状态
     * @param status 状态
     * @param ids id列表
     * @return
     */
    @Override
    public boolean changeStatusBatchById(int status, List<Long> ids) {
        if (ids == null || ids.size() == 0)
            return false;

        ArrayList<Dish> list = new ArrayList<>();
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(status);
            list.add(dish);
        }
        return this.updateBatchById(list);
    }

    @Override
    public void removeBatchById(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Dish::getId,ids);
        queryWrapper.eq(Dish::getStatus,1);

        int count = this.count(queryWrapper);
        if(count > 0){
            //如果不能删除，抛出一个业务异常
            throw new CustomException("菜品正在售卖中，不能删除");
        }
        this.removeByIds(ids);
    }
}
