package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbAction;

/**
 * @Entity com.example.emos.wx.db.pojo.TbAction
 */
public interface TbActionMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TbAction record);

    int insertSelective(TbAction record);

    TbAction selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TbAction record);

    int updateByPrimaryKey(TbAction record);

}
