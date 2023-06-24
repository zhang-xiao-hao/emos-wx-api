package com.example.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Author: itxiaohao
 * @date: 2023-05-26 22:00
 * @Description: 自定义防止XSS攻击的前端请求处理类
 * 对HttpServletRequestWrapper的一点想法，该类为HttpServletRequest的包装类，用到了包装器设计模式。
 * 具体地，HttpServletRequestWrapper实现ServletRequest接口的所有方法，我们想扩展或者修改ServletRequest接口中的方法时，
 * 只需要去重写覆盖掉HttpServletRequestWrapper相应的方法即可，而无需通过实现HttpServletRequest的所有接口。
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 转义请求参数（比如去掉HTML标签、特殊符号）<script>alert(123)</script> -> alert(123)
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (!StrUtil.hasEmpty(value)){
            // hutool工具类
            value = HtmlUtil.filter(value);
        }
        return value;
    }
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null){
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (!StrUtil.hasEmpty(value)){
                    value = HtmlUtil.filter(value);
                    values[i] = value;
                }
            }
        }
        return values;
    }
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameters =  super.getParameterMap();
        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        if (parameters != null){
            for (String key : parameters.keySet()) {
                String[] values = parameters.get(key);
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (!StrUtil.hasEmpty(value)){
                        value = HtmlUtil.filter(value);
                        values[i] = value;
                    }
                }
                map.put(key, values);
            }
        }
        return map;
    }
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 读io字节流到StringBuffer字符流
        InputStream in = super.getInputStream();
        InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader buffer = new BufferedReader(reader);
        StringBuffer body = new StringBuffer();
        String line = buffer.readLine();
        while (line != null){
            body.append(line);
            line = buffer.readLine();
        }
        buffer.close();
        reader.close();;
        in.close();

        // 转义
        Map<String, Object> map = JSONUtil.parseObj(body.toString());
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : map.keySet()) {
            Object val = map.get(key);
            if (val instanceof String){
                if (!StrUtil.hasEmpty(val.toString())){
                    result.put(key, HtmlUtil.filter(val.toString()));
                }
            }else {
                result.put(key, val);
            }
        }
        // 转为字节流返回
        String json = JSONUtil.toJsonStr(result);
        ByteArrayInputStream bain = new ByteArrayInputStream(json.getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bain.read();
            }
        };
    }

    /**
     * 转义请求头参数（比如转义HTML标签、特殊符号）
     */
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (!StrUtil.hasEmpty(value)){
            value = HtmlUtil.filter(value);
        }
        return value;
    }
}
