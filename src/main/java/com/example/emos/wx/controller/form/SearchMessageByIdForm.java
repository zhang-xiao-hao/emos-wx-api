package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: itxiaohao
 * @date: 2023-06-06 22:52
 * @Description:
 */
@ApiModel
@Data
public class SearchMessageByIdForm {
    @NotBlank
    private String id;
}
