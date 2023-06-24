package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Entity com.example.emos.wx.db.pojo.SysConfig
 */
@Mapper
public interface SysConfigMapper {
    List<SysConfig> selectAllParam();
}
