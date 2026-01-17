/* com/uhi/gourmet/wait/WaitService.java */
package com.uhi.gourmet.wait;

import java.util.List;
import java.util.Map; // 추가

public interface WaitService {
    // 1. 웨이팅 등록
    void register_wait(WaitVO vo);
    
    // 2. 현재 매장의 총 대기 팀 수 조회
    int get_current_wait_count(int store_id);
    
    // 3. 일반 회원: 내 웨이팅 목록 조회
    List<WaitVO> get_my_wait_list(String user_id);
    
    // 4. 점주: 매장별 웨이팅 목록 조회
    List<WaitVO> get_store_wait_list(int store_id);
    
    // 5. 웨이팅 상태 업데이트 (호출, 입장, 취소 등)
    void update_wait_status(int wait_id, String status);

    // [수정 포인트] 이용 현황 요약 정보 조회 (비즈니스 로직 규격화)
    Map<String, Object> getMyStatusSummary(String userId);
}