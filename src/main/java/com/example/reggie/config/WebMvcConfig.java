package com.example.reggie.config;

import com.example.reggie.common.JacksonObjectMapper;
import com.example.reggie.interceptor.BackendInterceptor;
import com.example.reggie.interceptor.FrontendInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    @Autowired
    private BackendInterceptor backendInterceptor;
    @Autowired
    private FrontendInterceptor frontendInterceptor;

    private final String backStaticSource = "/backend/**";
    private final String frontStaticSource = "/front/**";

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行静态资源映射...");
        registry.addResourceHandler(backStaticSource).addResourceLocations("classpath:/backend/");
        registry.addResourceHandler(frontStaticSource).addResourceLocations("classpath:/front/");
        super.addResourceHandlers(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("注册后台拦截器");
        ArrayList<String> backPath = new ArrayList<>();
        backPath.add("/employee/**");
        backPath.add("/category/**");
        backPath.add("/common/**");
        backPath.add("/dish/**");
        backPath.add("/setmeal/**");
        backPath.add("/order/page");
        backPath.add("/orderDetail/**");
        registry.addInterceptor(backendInterceptor).addPathPatterns(backPath)
                .excludePathPatterns("/employee/login", "/category/list", "/dish/list", "/setmeal/list", "/common/download", "/setmeal/dish/**");

        log.info("注册前台拦截器");
        ArrayList<String> frontPath = new ArrayList<>();
        frontPath.add("/user/**");
        frontPath.add("/addressBook/**");
        frontPath.add("/shoppingCart/**");
        frontPath.add("/order/submit");
        frontPath.add("/order/userPage");
        frontPath.add("/order/again");
        registry.addInterceptor(frontendInterceptor).addPathPatterns(frontPath).
                excludePathPatterns("/user/login", "/user/sendsms");
    }

    /**
     * 扩展mvc框架的消息转换器
     *
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用Jackson将Java对象转为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器对象追加到mvc框架的转换器集合中
        converters.add(0, messageConverter);
    }
}