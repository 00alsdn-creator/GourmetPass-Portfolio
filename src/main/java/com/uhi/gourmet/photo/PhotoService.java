package com.uhi.gourmet.photo;

import java.util.List;

public interface PhotoService {

    void addPhoto(PhotoVO vo);

    List<PhotoVO> getPhotosByStore(int store_id);

    List<PhotoVO> getPhotosByStoreAll(int store_id);

    PhotoVO getThumbnailByStore(int store_id);

    int getPhotoCountByStore(int store_id);

    void setThumbnail(int store_id, int photo_id);

    void deactivatePhoto(int photo_id);

    void activatePhoto(int photo_id);

    void deletePhoto(int photo_id);
}
