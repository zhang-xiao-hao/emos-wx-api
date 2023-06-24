package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbMeeting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Entity com.example.emos.wx.db.pojo.TbMeeting
 */
public interface TbMeetingMapper {
    int insertMeeting(TbMeeting entity);
    ArrayList<HashMap> searchMyMeetingListByPage(HashMap param);
    boolean searchMeetingMembersInSameDept(String uuid);
    int updateMeetingInstanceId(HashMap map);
    HashMap searchMeetingById(int id);
    ArrayList<HashMap> searchMeetingMembers(int id);
    int updateMeetingInfo(HashMap param);
    int deleteMeetingById(int id);
    List<String> searchUserMeetingInMonth(HashMap param);
}
