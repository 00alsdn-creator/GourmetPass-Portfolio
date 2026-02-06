/* com/uhi/gourmet/review/ReviewServiceImpl.java */
package com.uhi.gourmet.review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.uhi.gourmet.store.StoreService;
import com.uhi.gourmet.store.StoreVO;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper review_mapper;

    @Autowired
    private StoreService store_service;

    @Override
    public void registerReview(ReviewVO vo, String userId) {
        if (!checkReviewEligibility(userId, vo.getStore_id())) {
            throw new RuntimeException("방문 완료 후에만 리뷰를 작성할 수 있습니다.");
        }
        
        // 외래키 0값 방지 (데이터 정화)
        if (vo.getBook_id() != null && vo.getBook_id() == 0) vo.setBook_id(null);
        if (vo.getWait_id() != null && vo.getWait_id() == 0) vo.setWait_id(null);
        
        vo.setUser_id(userId);
        review_mapper.insertReview(vo);
    }

    @Override
    public PageInfo<ReviewVO> getStoreReviews(int store_id, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ReviewVO> list = review_mapper.selectStoreReviews(store_id);
        return new PageInfo<>(list);
    }

    // [수정] 내 리뷰 목록 페이징 구현
    @Override
    public PageInfo<ReviewVO> getMyReviewsPaginated(String user_id, int pageNum, int pageSize) {
        // PageHelper를 통해 쿼리 가로채기
        PageHelper.startPage(pageNum, pageSize);
        
        // Mapper의 기존 selectMyReviews를 그대로 호출해도 PageHelper가 페이징 쿼리로 변환함
        List<ReviewVO> list = review_mapper.selectMyReviews(user_id);
        
        return new PageInfo<>(list);
    }

    @Override
    public Map<String, Object> getReviewStats(int store_id) {
        return review_mapper.selectReviewStats(store_id);
    }
    
    @Override
    public boolean canDeleteReview(int review_id, String user_id) {
        String author = review_mapper.selectReviewAuthor(review_id);
        return author != null && author.equals(user_id);
    }

    @Override
    public void removeReview(int review_id) {
        review_mapper.deleteReview(review_id);
    }

    @Override
    public boolean checkReviewEligibility(String user_id, int store_id) {
        return review_mapper.countFinishedVisits(user_id, store_id) > 0;
    }

    @Override
    public Map<String, Object> getReviewWriteContext(String userId, int storeId, Integer bookId, Integer waitId) {
        Map<String, Object> context = new HashMap<>();
        if (!checkReviewEligibility(userId, storeId)) {
            context.put("isEligible", false);
            return context;
        }
        context.put("isEligible", true);
        context.put("store", store_service.getStoreDetail(storeId));
        context.put("book_id", bookId);
        context.put("wait_id", waitId);
        return context;
    }
}