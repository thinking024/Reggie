package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.dto.DishDto;
import com.example.reggie.dto.SetmealDto;
import com.example.reggie.pojo.*;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishService;
import com.example.reggie.service.SetmealDishService;
import com.example.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("setmeal")
@RestController
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired DishService dishService;

    /**
     * 根据id获取单个套餐信息，用于修改套餐页面
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        if (setmealDto == null)
            return R.error("未查询到此套餐信息");
        return R.success(setmealDto);
    }

    @GetMapping("/dish/{id}")
    public R<List<DishDto>> query(@PathVariable Long id) {
        LambdaQueryWrapper<SetmealDish> listLambdaQueryWrapper = new LambdaQueryWrapper<>();
        listLambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        log.info("显示的菜品为：" + listLambdaQueryWrapper);
        List<SetmealDish> list = setmealDishService.list(listLambdaQueryWrapper);

        ArrayList<Long> idList = new ArrayList<>();
        for (SetmealDish setmealDish : list) {
            idList.add(setmealDish.getDishId());
        }

        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Dish::getId, idList);
        List<Dish> dishList = dishService.list(wrapper);
        List<DishDto> dishDtoList = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getId, dishId);
            setmealDishLambdaQueryWrapper.eq(SetmealDish::getDishId, dishId);
            SetmealDish setmealDish = setmealDishService.getOne(setmealDishLambdaQueryWrapper);
            //SQL:select * from dish_flavor where dish_id = ?
            /*List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);*/
            dishDto.setCopies(setmealDish.getCopies());

            log.info("份数为：" + dishDto.getCopies());

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);

    }


    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            // 对象拷贝
            BeanUtils.copyProperties(item, setmealDto);
            //根据分类id查询分类对象，获取分类名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 根据类别id和状态查询套餐
     *
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);
        if (setmealService.saveWithDish(setmealDto))
            return R.success("新增套餐成功");
        return R.error("新增套餐失败");
    }

    /**
     * 修改套餐信息
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R update(@RequestBody SetmealDto setmealDto) {
        if (setmealService.updateWithDish(setmealDto))
            return R.success("修改套餐成功");
        return R.error("修改套餐失败");
    }

    /**
     * 批量修改套餐售卖状态
     *
     * @param type
     * @param ids
     * @return
     */
    @PostMapping("/status/{type}")
    public R changeStatus(@PathVariable int type, @RequestParam List<Long> ids) {
        log.info("type=" + type + "");
        log.info("id数据" + ids);
        if (setmealService.changeStatusBatchById(type, ids))
            return R.success("修改成功");
        return R.error("修改失败");
    }

    /**
     * 批量删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);
        setmealService.removeBatchById(ids);
        return R.success("套餐数据删除成功");
    }
}
