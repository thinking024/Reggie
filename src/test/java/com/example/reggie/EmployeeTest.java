package com.example.reggie;

import com.example.reggie.pojo.Employee;
import com.example.reggie.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmployeeTest {
    @Autowired
    private EmployeeService employeeService;

    @Test
    public void test() {
        Employee employee = new Employee();
        employee.setIdNumber("430111111111111111");
        employee.setName("yiyuanzhu");
        employee.setUsername("helloword");
        employee.setSex("1");
        employee.setPhone("13142338656");
        employeeService.save(employee);
        System.out.println(employee);
    }
}
