/* com/uhi/gourmet/wait/WaitService.java */
package com.uhi.gourmet.wait;

import java.util.List;
import java.util.Map;

public interface WaitService {
    void register_wait(WaitVO vo);
    int get_current_wait_count(int store_id);
    List<WaitVO> get_my_wait_list(String user_id);
    List<WaitVO> get_store_wait_list(int store_id);
    void update_wait_status(int wait_id, String status);
    Map<String, Object> getMyStatusSummary(String userId);

    // [추가] 특정 웨이팅 상세 정보 조회 (Controller에서 호출함)
    WaitVO get_wait_detail(int wait_id); 
    
 // [추가] 오늘 중복 웨이팅 체크
   boolean hasWaitingToday(String user_id, int store_id);
}