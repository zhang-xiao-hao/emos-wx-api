package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbDept;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @Entity com.example.emos.wx.db.pojo.TbDept
 */
public interface TbDeptMapper {

    ArrayList<HashMap> searchDeptMembers(String keyword);

}
