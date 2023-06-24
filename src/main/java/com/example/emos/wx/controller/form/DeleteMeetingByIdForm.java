package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @Author: itxiaohao
 * @date: 2023-06-18 22:03
 * @Description:
 */
@ApiModel
@Data
public class DeleteMeetingByIdForm {
    @NotNull
    @Min(1)
    private Integer id;
}
