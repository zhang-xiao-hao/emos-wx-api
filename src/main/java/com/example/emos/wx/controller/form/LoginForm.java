package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: itxiaohao
 * @date: 2023-05-30 23:50
 * @Description: 登录表单类
 */
@Data
@ApiModel
public class LoginForm {
    @NotBlank(message = "临时授权不能为空")
    private String code;
}
