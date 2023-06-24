package com.example.emos.wx.controller;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.controller.form.TestSayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author: itxiaohao
 * @date: 2023-05-26 16:43
 * @Description: swagger test
 */
@RestController
@RequestMapping("/test")
@Api("测试web接口") //swagger注解
public class TestController {
    @PostMapping("/sayHello")
    @ApiOperation("swagger测试方法")
    public R satHello(@Valid @RequestBody TestSayHelloForm form){
        return R.ok().put("message", "hello," + form.getName());
    }

    @PostMapping("/addUser")
    @ApiOperation("添加用户")
    @RequiresPermissions(value = {"ROOT", "USER:ADD"}, logical = Logical.OR)
    public R addUser(){
        return R.ok("success");
    }
}
