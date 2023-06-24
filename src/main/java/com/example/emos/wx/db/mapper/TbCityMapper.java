package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbCity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Entity com.example.emos.wx.db.pojo.TbCity
 */
@Mapper
public interface TbCityMapper {
    // 查询城市码
    String searchCode(String city);
}
