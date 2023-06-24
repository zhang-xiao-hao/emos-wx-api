package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * @Author: itxiaohao
 * @date: 2023-06-15 15:25
 * @Description:
 */
@Data
@ApiModel
public class SearchUserGroupByDeptForm {
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{1,15}$")//1-15个汉字
    private String keyword;
}
