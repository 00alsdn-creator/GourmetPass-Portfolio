/* com/uhi/gourmet/book/BookMapper.java */
package com.uhi.gourmet.book;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BookMapper {
    
    // 1. 예약 등록 (BookServiceImpl에서 호출)
    void insertBook(BookVO vo);

    // 2. 일반 회원: 내 예약 내역 조회
    List<BookVO> selectMyBookList(String user_id);

    // 3. 점주 회원: 매장별 예약자 명단 조회
    List<BookVO> selectStoreBookList(int store_id);
    
    // 4. 예약 상태 업데이트 (방문 완료, 노쇼 등)
    void updateBookStatus(@Param("book_id") int book_id, @Param("status") String status);

    /**
     * [신규] 5. 동일 시간대 중복 예약 확인
     * 기능: 특정 가게의 특정 날짜/시간에 이미 확정된 예약이 있는지 카운트합니다.
     * @param store_id 매장 번호
     * @param date 예약 날짜 (YYYY-MM-DD)
     * @param time 예약 시간 (HH:mm)
     * @return 예약 건수 (0이면 예약 가능, 1 이상이면 중복)
     */
    int checkDuplicateTime(@Param("store_id") int store_id, 
                           @Param("date") String date, 
                           @Param("time") String time);

    /**
     * [신규] 6. 사용자의 당일 중복 예약 확인
     * 기능: 특정 사용자가 특정 가게에 오늘 이미 예약을 했는지 확인하여 1인 1예약을 강제합니다.
     * @param store_id 매장 번호
     * @param user_id 사용자 아이디
     * @param date 예약 날짜 (YYYY-MM-DD)
     * @return 예약 건수 (0이면 예약 가능, 1 이상이면 이미 예약함)
     */
    int checkUserDailyBook(@Param("store_id") int store_id, 
                           @Param("user_id") String user_id, 
                           @Param("date") String date);

    // 점주전용 : 날짜별 예약 리스트
	List<BookVO> selectStoreBookListByDate(@Param("store_id")int store_id, @Param("book_date")String book_date);

}