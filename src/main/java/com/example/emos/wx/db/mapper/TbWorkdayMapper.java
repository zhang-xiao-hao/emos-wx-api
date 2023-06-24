package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbWorkday;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * @Entity com.example.emos.wx.db.pojo.TbWorkday
 */
@Mapper
public interface TbWorkdayMapper {
    // 查询当天是否为特殊工作日
    Integer searchTodayIsWorkdays();
    // 日期范围内的工作日
    List<String> searchWorkdayInRange(HashMap<String, Object> param);
}
