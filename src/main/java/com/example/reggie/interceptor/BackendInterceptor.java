package com.example.reggie.interceptor;

import com.alibaba.fastjson.JSON;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class BackendInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        //这里可以根据session的用户来判断角色的权限，根据权限来转发不同的页面
        log.info("后台请求地址:" + request.getRequestURI());
        if(request.getSession().getAttribute("employeeId") == null) {
            log.info("后台未登录");
            //如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
            response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
            return false;
        }
        log.info("成功放行后台");
        Long employeeId = (Long) request.getSession().getAttribute("employeeId");
        BaseContext.setCurrentId(employeeId);
        return true;
    }

//  pre返回true执行，在DispatcherServlet 渲染视图之前
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView)  {
        log.info("postHandle");
    }

//  pre返回true执行，在DispatcherServlet之后
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e)  {
        BaseContext.removeCurrentId();
        log.info("afterCompletion, remove employeeId in thread");
    }
}