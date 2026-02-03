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

        //  지금 진행 중인 웨이팅이 있으면 금지
        if (wait_mapper.existsUserActiveWaiting(vo.getUser_id()) > 0) {
            throw new IllegalStateException("이미 진행 중인 웨이팅이 있습니다.");
        }

        // 가게별 오늘 번호는 그대로
        Integer maxNum = wait_mapper.selectMaxWaitNum(vo.getStore_id());
        int nextNum = (maxNum == null) ? 1 : maxNum + 1;
        vo.setWait_num(nextNum);

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
    
    
    @Override
    public int getTeamsAheadToday(int store_id, int wait_num) {
        return wait_mapper.selectTeamsAhead(store_id, wait_num);
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