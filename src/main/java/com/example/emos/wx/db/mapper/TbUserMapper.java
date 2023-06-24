package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbUser;

import java.util.*;

/**
 * @Entity com.example.emos.wx.db.pojo.TbUser
 */
public interface TbUserMapper {
    boolean haveRootUser();
    int insert(Map<String, Object> param);
    int searchIdByOpenId(String openId);
    Set<String> searchUserPermissions(int userId);
    TbUser searchById(int userId);
    HashMap<String, String> searchNameAndDept(int userId);
    // 查询员工入职日期
    String searchUserHiredate(int userId);
    HashMap<String, Object> searchUserSummary(int userId);
    ArrayList<HashMap> searchUserGroupByDept(String keyword);
    ArrayList<HashMap> searchMembers(List param);
    HashMap searchUserInfo(int userId);
    int searchDeptManagerId(int id);
    int searchGmId();
    List<HashMap> selectUserPhotoAndName(List param);
    String searchMemberEmail(int id);
}
