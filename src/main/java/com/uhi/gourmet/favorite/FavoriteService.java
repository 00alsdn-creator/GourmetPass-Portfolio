package com.uhi.gourmet.favorite;

import java.util.List;

public interface FavoriteService {

    boolean isFavorite(String user_id, int store_id);

    boolean addFavorite(String user_id, int store_id);

    boolean removeFavorite(String user_id, int store_id);

    int getFavoriteCountByStore(int store_id);

    List<FavoriteVO> getFavoritesByUser(String user_id);
}
