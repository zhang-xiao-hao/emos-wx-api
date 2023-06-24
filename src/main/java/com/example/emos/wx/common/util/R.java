package com.example.emos.wx.common.util;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: itxiaohao
 * @date: 2023-05-26 16:47
 * @Description: web返回值封装类
 */
public class R extends HashMap<String, Object> {
    // 默认200
    private R(){
        put("code", HttpStatus.SC_OK);
        put("msg", "success");
    }
    // 声明一个可以链式调用的put方法
    public R put(String key, Object value){
        super.put(key, value);
        return this;
    }
    public static R ok(){
        return new R();
    }
    public static R ok(String msg){
        R r = new R();
        r.put("msg", msg);
        return r;
    }
    public static R ok(Map<String, Object> map){
        R r = new R();
        r.putAll(map);
        return r;
    }
    // 自定义状态码
    public static R error(int code, String msg){
        R r = new R();
        r.put("msg", msg);
        r.put("code", code);
        return r;
    }
    // 500状态码
    public static R error(String msg){
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
    }
    public static R error(){
        return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
    }
}
