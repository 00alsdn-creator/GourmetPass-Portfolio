package com.uhi.gourmet.review;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReviewMapper {
    // 1. 리뷰 등록
    void insertReview(ReviewVO vo);

    // 2. 특정 가게의 리뷰 목록 조회 (PageHelper가 가로채어 페이징 처리)
    List<ReviewVO> selectStoreReviews(@Param("store_id") int store_id);

    // 3. 내가 작성한 리뷰 목록 조회
    List<ReviewVO> selectMyReviews(@Param("user_id") String user_id);

    // 4. 가게별 리뷰 통계 (평균 별점, 총 리뷰 수)
    Map<String, Object> selectReviewStats(@Param("store_id") int store_id);
    
    // 5. 리뷰 삭제
    void deleteReview(@Param("review_id") int review_id);

    // 6. 방문 완료(예약 또는 웨이팅) 횟수 조회 (리뷰 작성 자격 확인용)
    int countFinishedVisits(@Param("user_id") String user_id, @Param("store_id") int store_id);
}