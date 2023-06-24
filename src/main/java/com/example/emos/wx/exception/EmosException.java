package com.example.emos.wx.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: itxiaohao
 * @date: 2023-05-26 13:28
 * @Description: 自定义异常类，RuntimeException可以不用捕获
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EmosException extends RuntimeException{
    private String msg;
    private int code = 500;

    public EmosException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public EmosException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public EmosException(String msg, int code) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public EmosException(String msg, int code, Throwable e) {
        super(msg, e);
        this.code = code;
        this.msg = msg;
    }
}
