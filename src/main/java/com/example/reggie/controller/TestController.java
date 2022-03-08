package com.example.reggie.controller;

import com.example.reggie.common.R;
import org.springframework.web.bind.annotation.*;


@RequestMapping("test")
@RestController
public class TestController {
    @GetMapping("gettest")
    public R getTest() {
        return R.success(null);
    }
}
