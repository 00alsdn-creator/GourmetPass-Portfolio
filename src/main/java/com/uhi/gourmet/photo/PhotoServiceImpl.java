package com.uhi.gourmet.photo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PhotoServiceImpl implements PhotoService {

    @Autowired
    private PhotoMapper photoMapper;

    @Override
    public void addPhoto(PhotoVO vo) {
        photoMapper.insertPhoto(vo);
    }

    @Override
    public List<PhotoVO> getPhotosByStore(int store_id) {
        return photoMapper.selectPhotosByStore(store_id);
    }

    @Override
    public List<PhotoVO> getPhotosByStoreAll(int store_id) {
        return photoMapper.selectPhotosByStoreAll(store_id);
    }

    @Override
    public PhotoVO getThumbnailByStore(int store_id) {
        return photoMapper.selectThumbnailByStore(store_id);
    }

    @Override
    public int getPhotoCountByStore(int store_id) {
        return photoMapper.selectPhotoCountByStore(store_id);
    }

    @Override
    public void setThumbnail(int store_id, int photo_id) {
        photoMapper.clearThumbnailByStore(store_id);
        photoMapper.setThumbnail(photo_id);
    }

    @Override
    public void deactivatePhoto(int photo_id) {
        photoMapper.deactivatePhoto(photo_id);
    }

    @Override
    public void activatePhoto(int photo_id) {
        photoMapper.activatePhoto(photo_id);
    }

    @Override
    public void deletePhoto(int photo_id) {
        photoMapper.deletePhoto(photo_id);
    }
}
