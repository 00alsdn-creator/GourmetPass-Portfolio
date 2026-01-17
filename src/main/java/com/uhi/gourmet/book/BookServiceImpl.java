/* com/uhi/gourmet/book/BookServiceImpl.java */
package com.uhi.gourmet.book;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    @Autowired
    private BookMapper book_mapper;

    // [수정] 하드코딩된 변수를 api.properties 설정값으로 분리 (보안 및 유연성)
    @Value("${book.debug.mode:true}")
    private boolean is_debug_mode;

    @Override
    public void register_book(BookVO vo, String date, String time) {
        try {
            // [수정] 컨트롤러에 있던 데이터 가공 로직을 서비스로 이전 (관심사 분리)
            String full_date = date + " " + time; // yyyy-MM-dd HH:mm
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date combined_date = sdf.parse(full_date);
            vo.setBook_date(combined_date);
            
            if (is_debug_mode) {
                System.out.println("DEBUG: 결제 검증 스위치 ON - 예약 바로 진행");
            }
            
            book_mapper.insertBook(vo);
            
        } catch (Exception e) {
            // 날짜 파싱 오류 발생 시 런타임 예외로 변환하여 트랜잭션 롤백 유도
            throw new RuntimeException("예약 날짜 처리 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public List<BookVO> get_my_book_list(String user_id) {
        return book_mapper.selectMyBookList(user_id);
    }

    @Override
    public List<BookVO> get_store_book_list(int store_id) {
        return book_mapper.selectStoreBookList(store_id);
    }

    @Override
    public void update_book_status(int book_id, String status) {
        book_mapper.updateBookStatus(book_id, status);
    }
}