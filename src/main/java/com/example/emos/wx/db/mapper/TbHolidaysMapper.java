package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbHolidays;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * @Entity com.example.emos.wx.db.pojo.TbHolidays
 */
@Mapper
public interface TbHolidaysMapper {
    // 查询当天是否为特殊休息日
    Integer searchTodayIsHolidays();
    // 日期范围内的特殊节假日
    List<String> searchHolidaysInRange(HashMap<String, Object> param);
}
