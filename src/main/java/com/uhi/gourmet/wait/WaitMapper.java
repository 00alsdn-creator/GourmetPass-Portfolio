/* com/uhi/gourmet/wait/WaitMapper.java */
package com.uhi.gourmet.wait;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WaitMapper {
    
    // 1. 웨이팅 등록 (WaitServiceImpl에서 호출)
    void insertWait(WaitVO vo);

    // 2. 현재 매장의 가장 높은 대기 번호 조회 (새 번호 발급용)
    Integer selectMaxWaitNum(int store_id);

    // 3. 매장별 현재 대기 팀수 조회 (오늘 기준)
    int selectCurrentWaitCount(int store_id);

    // 4. 일반 회원: 내 웨이팅 내역 조회 (MemberController 마이페이지용)
    List<WaitVO> selectMyWaitList(String user_id);

    // 5. 점주: 특정 매장의 전체 웨이팅 목록 조회 (BookController/manage용)
    List<WaitVO> selectStoreWaitList(int store_id);
    
    // 6. 웨이팅 상태 업데이트 (호출, 입장완료, 취소 등)
    // 매개변수가 2개이므로 @Param을 사용하여 XML의 #{wait_id}, #{status}와 연결합니다.
    void updateWaitStatus(@Param("wait_id") int wait_id, @Param("status") String status);

    /**
     * [핵심 추가] 7. PK로 웨이팅 정보 단건 조회
     * 역할: 점주가 상태 변경 시, 해당 웨이팅의 store_id를 파악하여 
     * 전체 대기자에게 실시간 방송(Broadcast) 신호를 보내기 위해 필요합니다.
     */
    WaitVO selectWaitDetail(int wait_id);
    
    int existsWaitingToday(
            @Param("user_id") String user_id,
            @Param("store_id") int store_id
        );
    
    int selectTeamsAhead(
    		@Param("store_id") int store_id, 
    		@Param("wait_num") int wait_num);
    
    // 여러가게 동시 웨이팅 방지
    int existsUserActiveWaiting(@Param("user_id") String user_id);

}