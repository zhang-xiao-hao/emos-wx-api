package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: itxiaohao
 * @date: 2023-06-11 21:33
 * @Description:
 */
public interface MeetingService {
    void insertMeeting(TbMeeting entity);
    ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);
    HashMap searchMeetingById(int id);
    void updateMeetingInfo(HashMap param);
    void deleteMeetingById(int id);
    long searchRoomIdByUUID(String uuid);
    List<String> searchUserMeetingInMonth(HashMap param);
}
