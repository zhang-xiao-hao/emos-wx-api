package com.example.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

/**
 * @Author: itxiaohao
 * @date: 2023-05-27 21:25
 * @Description: token的媒介类，用来传递token
 */
@Component
public class ThreadLocalToken {
    private final ThreadLocal<String> local = new ThreadLocal<>();

    public void setToken(String token){
        local.set(token);
    }

    public String getToken(){
        return local.get();
    }

    public void clear(){
        local.remove();
    }
}
