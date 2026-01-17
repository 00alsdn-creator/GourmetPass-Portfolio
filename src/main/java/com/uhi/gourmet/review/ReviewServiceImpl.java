/* com/uhi/gourmet/review/ReviewServiceImpl.java */
package com.uhi.gourmet.review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uhi.gourmet.store.StoreService;
import com.uhi.gourmet.store.StoreVO;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper review_mapper;

    @Autowired
    private StoreService store_service;

    // [수정 포인트: 비즈니스 검증 로직을 서비스 내부로 흡수]
    @Override
    public void registerReview(ReviewVO vo, String userId) {
        // 1. 권한 검증
        if (!checkReviewEligibility(userId, vo.getStore_id())) {
            throw new RuntimeException("방문 완료 후에만 리뷰를 작성할 수 있습니다.");
        }
        
        // 2. 데이터 세팅 및 저장
        vo.setUser_id(userId);
        review_mapper.insertReview(vo);
    }

    @Override
    public List<ReviewVO> getStoreReviews(int store_id) {
        return review_mapper.selectStoreReviews(store_id);
    }

    @Override
    public List<ReviewVO> getMyReviews(String user_id) {
        return review_mapper.selectMyReviews(user_id);
    }

    @Override
    public Map<String, Object> getReviewStats(int store_id) {
        return review_mapper.selectReviewStats(store_id);
    }

    @Override
    public void removeReview(int review_id) {
        review_mapper.deleteReview(review_id);
    }

    @Override
    public boolean checkReviewEligibility(String user_id, int store_id) {
        int count = review_mapper.countFinishedVisits(user_id, store_id);
        return count > 0;
    }

    // [수정 포인트: 컨트롤러의 데이터 가공 책임을 서비스로 이전]
    @Override
    public Map<String, Object> getReviewWriteContext(String userId, int storeId, Integer bookId, Integer waitId) {
        Map<String, Object> context = new HashMap<>();
        
        // 자격 검증
        if (!checkReviewEligibility(userId, storeId)) {
            context.put("isEligible", false);
            return context;
        }

        // 가게 정보 및 요청 파라미터 조합
        StoreVO store = store_service.getStoreDetail(storeId);
        context.put("isEligible", true);
        context.put("store", store);
        context.put("book_id", bookId);
        context.put("wait_id", waitId);
        
        return context;
    }
}