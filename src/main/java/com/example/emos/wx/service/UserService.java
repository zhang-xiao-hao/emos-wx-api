package com.example.emos.wx.service;

import com.example.emos.wx.db.pojo.TbUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @Author: itxiaohao
 * @date: 2023-05-30 21:18
 * @Description:
 */
public interface UserService {
    int registerUser(String registerCode, String code, String nickname, String photo);
    Set<String> searchUserPermissions(int userId);
    Integer login(String code);
    TbUser searchById(int userId);
    String searchUserHiredate(int userId);
    HashMap<String, Object> searchUserSummary(int userId);
    ArrayList<HashMap> searchUserGroupByDept(String keyword);
    ArrayList<HashMap> searchMembers(List param);
    List<HashMap> selectUserPhotoAndName(List param);
    String searchMemberEmail(int id);
}
