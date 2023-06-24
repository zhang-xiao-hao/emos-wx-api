package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbCheckin;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Entity com.example.emos.wx.db.pojo.TbCheckin
 */
@Mapper
public interface TbCheckinMapper {
    // 查询当天用户是否已经签到
    Integer haveCheckin(Map<String, Object> param);
    // 签到
    void insert(TbCheckin checkin);
    // 今日签到情况
    HashMap<String, Object> searchTodayCheckin(int userId);
    // 签到总天数
    long searchCheckinDays(int userId);
    //  日期范围内的签到情况
    List<HashMap<String, Object>> searchWeekCheckin(HashMap<String, Object> param);
}
