package com.example.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @Author: itxiaohao
 * @date: 2023-06-02 14:51
 * @Description:
 */
@Data
@ApiModel
public class CheckinForm {
    public String address;
    public String country;
    public String province;
    public String city;
    public String district;
}
