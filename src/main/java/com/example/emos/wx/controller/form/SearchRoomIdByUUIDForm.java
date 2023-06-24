package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @Author: itxiaohao
 * @date: 2023-06-20 21:46
 * @Description:
 */
@ApiModel
@Data
public class SearchRoomIdByUUIDForm {
    @NotBlank
    private String uuid;
}
