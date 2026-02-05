/* com/uhi/gourmet/review/ReviewService.java */
package com.uhi.gourmet.review;

import java.util.List;
import java.util.Map;
import com.github.pagehelper.PageInfo;

public interface ReviewService {
    // 1. 리뷰 등록 (비즈니스 검증 및 데이터 정화 로직 포함)
    void registerReview(ReviewVO vo, String userId);

    // 2. 특정 가게의 리뷰 목록 조회 (페이징 적용)
    PageInfo<ReviewVO> getStoreReviews(int store_id, int pageNum, int pageSize);

    // 3. [수정] 내가 작성한 리뷰 목록 조회 (페이징 적용)
    // 기존 List<ReviewVO> getMyReviews(String user_id)를 대체하거나 함께 사용할 수 있습니다.
    PageInfo<ReviewVO> getMyReviewsPaginated(String user_id, int pageNum, int pageSize);

    // 4. 가게별 리뷰 통계 (평균 별점, 총 리뷰 수)
    Map<String, Object> getReviewStats(int store_id);

    // 5. 리뷰 삭제
    void removeReview(int review_id);

    // 6. 특정 사용자의 특정 가게 방문 완료 여부 체크
    boolean checkReviewEligibility(String user_id, int store_id);

    // 리뷰 작성 페이지용 컨텍스트 조회
    Map<String, Object> getReviewWriteContext(String userId, int storeId, Integer bookId, Integer waitId);
}