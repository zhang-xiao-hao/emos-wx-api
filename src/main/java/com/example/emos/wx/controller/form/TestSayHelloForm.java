package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Author: itxiaohao
 * @date: 2023-05-26 17:19
 * @Description: 后端数据验证
 */
@Data
@ApiModel(description = "用户信息")
public class TestSayHelloForm {
    @NotBlank
    @Pattern(regexp = "^[\\u4e00-\\u9fa5]{2,15}$") // 2-15个汉字
    @ApiModelProperty("姓名")
    private String name;
}
