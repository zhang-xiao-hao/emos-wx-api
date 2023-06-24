package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: itxiaohao
 * @date: 2023-06-16 22:55
 * @Description:
 */
@Data
@ApiModel
public class SearchMembersForm {
    @NotBlank
    private String members;
}
