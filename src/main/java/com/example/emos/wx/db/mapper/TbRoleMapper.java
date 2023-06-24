package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbRole;

/**
 * @Entity com.example.emos.wx.db.pojo.TbRole
 */
public interface TbRoleMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TbRole record);

    int insertSelective(TbRole record);

    TbRole selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TbRole record);

    int updateByPrimaryKey(TbRole record);

}
