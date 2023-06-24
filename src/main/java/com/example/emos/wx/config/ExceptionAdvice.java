package com.example.emos.wx.config;

import com.example.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: itxiaohao
 * @date: 2023-05-28 22:23
 * @Description: 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
//    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String validExceptionHandler(Exception e){
        log.error("执行异常", e);
        // 后端验证异常（参数等）
        if (e instanceof MethodArgumentNotValidException){
            MethodArgumentNotValidException exception = (MethodArgumentNotValidException)e;
            return exception.getBindingResult().getFieldError().getDefaultMessage();
        }else if (e instanceof EmosException){
            EmosException exception = (EmosException)e;
            return exception.getMsg();
        }else if (e instanceof UnauthorizedException){
            return "你不具备相关权限";
        }else {
            return "后端执行异常";
        }
    }
}
