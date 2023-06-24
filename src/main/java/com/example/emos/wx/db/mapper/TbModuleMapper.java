package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbModule;

/**
 * @Entity com.example.emos.wx.db.pojo.TbModule
 */
public interface TbModuleMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TbModule record);

    int insertSelective(TbModule record);

    TbModule selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TbModule record);

    int updateByPrimaryKey(TbModule record);

}
