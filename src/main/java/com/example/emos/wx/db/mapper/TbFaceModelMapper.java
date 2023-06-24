package com.example.emos.wx.db.mapper;

import com.example.emos.wx.db.pojo.TbFaceModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Entity com.example.emos.wx.db.pojo.TbFaceModel
 */
@Mapper
public interface TbFaceModelMapper {
    String searchFaceModel(int userId);
    void insert(TbFaceModel faceModel);
    int deleteFaceModel(int userId);
}
