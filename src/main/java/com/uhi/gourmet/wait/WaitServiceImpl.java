/* com/uhi/gourmet/wait/WaitServiceImpl.java */
package com.uhi.gourmet.wait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uhi.gourmet.book.BookService; // 추가
import com.uhi.gourmet.book.BookVO;

@Service
@Transactional
public class WaitServiceImpl implements WaitService {

    @Autowired
    private WaitMapper wait_mapper;

    @Autowired
    private BookService book_service; // [수정 포인트: 도메인 간 협력을 위한 서비스 주입]
    
    @Override
    public boolean hasWaitingToday(String user_id, int store_id) {
        return wait_mapper.existsWaitingToday(user_id, store_id) > 0;
    }
    
    @Override
    public synchronized void register_wait(WaitVO vo) {
    	
        // 오늘 중복 웨이팅 체크
        if (wait_mapper.existsWaitingToday(vo.getUser_id(), vo.getStore_id()) > 0) {
            throw new IllegalStateException("이미 오늘 웨이팅이 있습니다.");
        }

        // 오늘 가게별 다음 번호 계산
        Integer max_num = wait_mapper.selectMaxWaitNum(vo.getStore_id());
        int next_num = (max_num == null) ? 1 : max_num + 1;
        vo.setWait_num(next_num);

        // 등록
        wait_mapper.insertWait(vo);
    }

    @Override
    public int get_current_wait_count(int store_id) {
        return wait_mapper.selectCurrentWaitCount(store_id);
    }

    @Override
    public List<WaitVO> get_my_wait_list(String user_id) {
        return wait_mapper.selectMyWaitList(user_id);
    }

    @Override
    public List<WaitVO> get_store_wait_list(int store_id) {
        return wait_mapper.selectStoreWaitList(store_id);
    }

    @Override
    public void update_wait_status(int wait_id, String status) {
        wait_mapper.updateWaitStatus(wait_id, status);
    }

    // [수정 포인트: Controller의 비즈니스 로직(필터링, 요약) 이전]
    @Override
    public Map<String, Object> getMyStatusSummary(String userId) {
        Map<String, Object> summary = new HashMap<>();
        
        List<WaitVO> my_wait_list = wait_mapper.selectMyWaitList(userId);
        List<BookVO> my_book_list = book_service.get_my_book_list(userId);
        
        if (my_wait_list == null) my_wait_list = new ArrayList<>();
        if (my_book_list == null) my_book_list = new ArrayList<>();

        // 1. 진행 중인 내역 필터링
        summary.put("activeWait", my_wait_list.stream()
                .filter(w -> "WAITING".equals(w.getWait_status()) || "CALLED".equals(w.getWait_status()))
                .findFirst().orElse(null));
        
        summary.put("activeBook", my_book_list.stream()
                .filter(b -> "RESERVED".equals(b.getBook_status()))
                .findFirst().orElse(null));

        // 2. 리뷰 작성이 가능한 완료 내역(FINISH) 추출
        summary.put("finishedWaits", my_wait_list.stream()
                .filter(w -> "FINISH".equals(w.getWait_status()))
                .collect(Collectors.toList()));
        
        summary.put("finishedBooks", my_book_list.stream()
                .filter(b -> "FINISH".equals(b.getBook_status()))
                .collect(Collectors.toList()));

        summary.put("my_wait_list", my_wait_list);
        summary.put("my_book_list", my_book_list);
        
        return summary;
    }

    /**
     * [추가] 특정 웨이팅의 상세 정보를 가져오는 메서드
     * 역할: DB에서 wait_id로 정보를 조회하여 Mapper로부터 VO를 전달받음
     */
    @Override
    public WaitVO get_wait_detail(int wait_id) {
        return wait_mapper.selectWaitDetail(wait_id);
    }
}