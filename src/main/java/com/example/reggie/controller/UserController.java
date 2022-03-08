package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import com.example.reggie.pojo.User;
import com.example.reggie.service.UserService;
import com.example.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/test")
    public R test() {
       return R.error("test");
    }

    /**
     * 发送手机短信验证码
     *
     * @param user
     * @return
     */
    @PostMapping("/sendsms")
    public R<String> sendSms(@RequestBody User user, HttpServletRequest request) {
        // 获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);
            // 需要将生成的验证码保存到Session
            request.getSession().setAttribute(phone, code);
            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     * 移动端用户登录
     *
     * @param map
     * @param
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String, String> map, HttpServletRequest request) {
        String phone = map.get("phone");
        String code = map.get("code");
        Object codeInSession = request.getSession().getAttribute(phone);
        log.error("codeInSession=" + codeInSession + ", " + "code="+ code);
        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）如果能够比对成功，说明登录成功
        if (codeInSession != null && codeInSession.equals(code)) {
            //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            request.getSession().setAttribute("userId", user.getId());
            return R.success(user);
        }
        return R.error("验证码错误，登录失败");
    }

    @PostMapping("logout")
    public R logout(HttpServletRequest request) {
        request.getSession().removeAttribute("userId");
        return R.success(null);
    }
}