/* com/uhi/gourmet/book/BookService.java */
package com.uhi.gourmet.book;

import java.util.List;

public interface BookService {
    

    void register_book(BookVO vo, String date, String time);
    
    // 일반 회원: 내 예약 목록 조회
    List<BookVO> get_my_book_list(String user_id);
    
    // 점주: 매장별 예약 목록 조회
    List<BookVO> get_store_book_list(int store_id);
    
    // 예약 상태 업데이트 (방문 완료, 노쇼 등)
    void update_book_status(int book_id, String status);
}