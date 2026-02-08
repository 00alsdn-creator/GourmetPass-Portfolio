package com.uhi.gourmet.favorite;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteMapper {

    //1. 즐겨찾기 여부 확인
    int selectFavoriteCount(@Param("user_id") String user_id, @Param("store_id") int store_id);

    //1-1. 매장 즐겨찾기 수
    int selectFavoriteCountByStore(@Param("store_id") int store_id);

    //2. 즐겨찾기 추가
    int insertFavorite(@Param("user_id") String user_id, @Param("store_id") int store_id);

    //3. 즐겨찾기 삭제
    int deleteFavorite(@Param("user_id") String user_id, @Param("store_id") int store_id);

    //4. 내 즐겨찾기 목록
    List<FavoriteVO> selectFavoritesByUser(String user_id);

}
