package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbPermission;

/**
 * @Entity com.example.emos.wx.db.pojo.TbPermission
 */
public interface TbPermissionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TbPermission record);

    int insertSelective(TbPermission record);

    TbPermission selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TbPermission record);

    int updateByPrimaryKey(TbPermission record);

}
