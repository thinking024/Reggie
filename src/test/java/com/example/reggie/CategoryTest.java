package com.example.reggie;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.reggie.pojo.Category;
import com.example.reggie.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CategoryTest {
    @Autowired
    private CategoryService categoryService;

    @Test
    public void test() {
        boolean flag = categoryService.removeById(1);
        System.out.println(flag);
    }
}
