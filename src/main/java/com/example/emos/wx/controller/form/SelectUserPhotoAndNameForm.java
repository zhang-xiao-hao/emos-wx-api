package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: itxiaohao
 * @date: 2023-06-19 22:50
 * @Description:
 */
@Data
@ApiModel
public class SelectUserPhotoAndNameForm {
    @NotBlank
    private String ids;
}
