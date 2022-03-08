package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import com.example.reggie.pojo.Employee;
import com.example.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@RequestMapping("employee")
@RestController
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // RequestBody将请求体中的请求json字符串转为java对象
    @PostMapping("login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request) {
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, employee.getUsername());
        wrapper.eq(Employee::getPassword, password);
        Employee one = employeeService.getOne(wrapper);
        if (one == null) {
            return R.error("用户名或密码错误");
        }
        if (one.getStatus() == 0) {
            return R.error("用户已被禁用");
        }
        request.getSession().setAttribute("employeeId", one.getId());
        return R.success(one); // 返回到前端，作为localstorage中的userInfo
    }

    @PostMapping("logout")
    public R logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employeeId");
        return R.success(null);
    }

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }

    /**
     * 员工信息分页查询
     *
     * @param page     当前查询页码
     * @param pageSize 每页展示记录数
     * @param name     员工姓名 - 可选参数
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);
        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件，传入name时才去查询，否则不添加name查询条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @PostMapping
    public R<String> save(@RequestBody Employee employee) {
        log.info("新增员工，员工信息：{}", employee.toString());

        //设置初始密码123456，需要进行md5加密处理
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        if (employeeService.save(employee))
            return R.success("新增员工成功");
        return R.error("新增员工失败");
    }

    /**
     * 根据id修改员工信息
     *
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee) {
        log.info(employee.toString());
        if (employee.getStatus() == 0) {
            if (BaseContext.getCurrentId() != 1) {
                return R.error("管理员才能禁用账户");
            }
            if (employee.getId() == BaseContext.getCurrentId()) {
                return R.error("无法禁用管理员账户");
            }
        }
        if (employeeService.updateById(employee))
            return R.success("员工信息修改成功");
        return R.error("修改失败");
    }
}