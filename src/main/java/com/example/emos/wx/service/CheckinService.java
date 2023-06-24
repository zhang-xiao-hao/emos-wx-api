package com.example.emos.wx.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: itxiaohao
 * @date: 2023-05-31 22:45
 * @Description:
 */
public interface CheckinService {
    // 是否可以签到
    String validCanCheckin(int userId, String date);
    void checkin(Map<String, Object> param);
    void createFaceModel(int userId, String path);

    HashMap<String, Object> searchTodayCheckin(int userId);
    long searchCheckinDays(int userId);
    List<HashMap<String, Object>> searchWeekCheckin(HashMap<String, Object> param);
    List<HashMap<String, Object>> searchMonthCheckin(HashMap<String, Object> param);
}
