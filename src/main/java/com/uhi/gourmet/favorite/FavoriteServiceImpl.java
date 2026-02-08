package com.uhi.gourmet.favorite;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Override
    public boolean isFavorite(String user_id, int store_id) {
        return favoriteMapper.selectFavoriteCount(user_id, store_id) > 0;
    }

    @Override
    public boolean addFavorite(String user_id, int store_id) {
        if (isFavorite(user_id, store_id)) {
            return false;
        }
        return favoriteMapper.insertFavorite(user_id, store_id) > 0;
    }

    @Override
    public boolean removeFavorite(String user_id, int store_id) {
        return favoriteMapper.deleteFavorite(user_id, store_id) > 0;
    }

    @Override
    public int getFavoriteCountByStore(int store_id) {
        return favoriteMapper.selectFavoriteCountByStore(store_id);
    }

    @Override
    public List<FavoriteVO> getFavoritesByUser(String user_id) {
        return favoriteMapper.selectFavoritesByUser(user_id);
    }
}
