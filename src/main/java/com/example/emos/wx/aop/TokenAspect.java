package com.example.emos.wx.aop;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author: itxiaohao
 * @date: 2023-05-28 21:49
 * @Description: token切面类
 */
@Aspect
@Component
public class TokenAspect {
    @Resource
    private ThreadLocalToken threadLocalToken;

    @Pointcut("execution(public * com.example.emos.wx.controller.*.*(..))")
    public void aspect(){}

    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable{
        R r = (R)point.proceed(); // 目标方法执行结果
        // 如果token刷新了，返回给用户新的token
        String token = threadLocalToken.getToken();
        if (token != null){
            r.put("token", token); // 往响应中放置Token
            threadLocalToken.clear();
        }
        return r;
    }
}
