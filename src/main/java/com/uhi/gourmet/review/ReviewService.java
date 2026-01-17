/* com/uhi/gourmet/review/ReviewService.java */
package com.uhi.gourmet.review;

import java.util.List;
import java.util.Map;

public interface ReviewService {
    // 1. 리뷰 등록 (비즈니스 검증 로직 포함)
    void registerReview(ReviewVO vo, String userId);

    // 2. 특정 가게의 리뷰 목록 조회
    List<ReviewVO> getStoreReviews(int store_id);

    // 3. 내가 작성한 리뷰 목록 조회
    List<ReviewVO> getMyReviews(String user_id);

    // 4. 가게별 리뷰 통계 (평균 별점, 총 리뷰 수)
    Map<String, Object> getReviewStats(int store_id);

    // 5. 리뷰 삭제
    void removeReview(int review_id);

    // 6. 특정 사용자의 특정 가게 방문 완료(FINISH) 여부 체크
    boolean checkReviewEligibility(String user_id, int store_id);

    // [수정 포인트] 리뷰 작성 페이지에 필요한 컨텍스트 데이터(가게정보, 권한 등) 일괄 조회
    Map<String, Object> getReviewWriteContext(String userId, int storeId, Integer bookId, Integer waitId);
}