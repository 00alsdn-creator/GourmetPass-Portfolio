package com.uhi.gourmet.photo;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PhotoMapper {

    void insertPhoto(PhotoVO vo);

    List<PhotoVO> selectPhotosByStore(@Param("store_id") int store_id);

    List<PhotoVO> selectPhotosByStoreAll(@Param("store_id") int store_id);

    PhotoVO selectThumbnailByStore(@Param("store_id") int store_id);

    int selectPhotoCountByStore(@Param("store_id") int store_id);

    int clearThumbnailByStore(@Param("store_id") int store_id);

    int setThumbnail(@Param("photo_id") int photo_id);

    int deactivatePhoto(@Param("photo_id") int photo_id);

    int activatePhoto(@Param("photo_id") int photo_id);

    int deletePhoto(@Param("photo_id") int photo_id);
}
