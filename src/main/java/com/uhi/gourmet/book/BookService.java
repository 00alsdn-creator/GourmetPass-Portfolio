/* com/uhi/gourmet/book/BookService.java */
package com.uhi.gourmet.book;

import java.util.List;
import java.util.Map;

public interface BookService {
    
	// 중복예약 조회
    int checkDuplicateTime(int storeId, String userId, String date, String time);
    
    // 1일(day)당 하나의 예약만 가능하도록 조회
    int checkUserDailyBook(int storeId, String userId, String date);
    
    void register_book(BookVO vo);
    
    // 일반 회원: 내 예약 목록 조회
    List<BookVO> get_my_book_list(String user_id);
    
    // 점주: 매장별 예약 목록 조회
    List<BookVO> get_store_book_list(int store_id);
    
    // 예약 상태 업데이트 (방문 완료, 노쇼, 캔슬 등)
    void update_book_status(int book_id, String status);


	List<BookVO> get_store_book_list_by_date(int store_id, String bookDate);
    
}