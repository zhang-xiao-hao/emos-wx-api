package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * @Author: itxiaohao
 * @date: 2023-06-20 22:17
 * @Description:
 */
@Data
@ApiModel
public class SearchUserMeetingInMonthForm {
    @Range(min = 2000, max = 9999)
    private Integer year;
    @Range(min = 1, max = 12)
    private Integer month;
}
