/* com/uhi/gourmet/book/BookServiceImpl.java */
package com.uhi.gourmet.book;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookServiceImpl implements BookService {

	@Autowired
	private BookMapper book_mapper;

	@Value("${book.debug.mode:true}")
	private boolean is_debug_mode;
	
	
	/* book 추가 */
	@Override
	public void register_book(BookVO vo) {
		// 1. 이미 Date 객체인 getBook_date()를 다시 "yyyy-MM-dd" 문자열로 변환
	    SimpleDateFormat dateOnlySdf = new SimpleDateFormat("yyyy-MM-dd");
	    String dateStr = dateOnlySdf.format(vo.getBook_date()); // "2026-01-21"
	    
	    // 2. 시간과 합치기
	    String full_date = dateStr + " " + vo.getBook_time(); // "2026-01-21 17:00"
	    
	    SimpleDateFormat fullSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	    try {
	        Date combined_date = fullSdf.parse(full_date);
	        vo.setBook_date(combined_date); // 시/분까지 포함된 객체로 덮어쓰기
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    
	    book_mapper.insertBook(vo);
	}


	/**
	 * [핵심 리팩토링] 예약 등록 및 중복 차단 로직 1. 날짜/시간 병합 및 파싱 2. 동일 시간대 중복 예약 검증 (타임슬롯당 1인 제한)
	 * 3. 동일 유저의 당일 중복 예약 검증 (1인 1일 1회 제한)
	 */
	@Override
	public int checkDuplicateTime(int storeId,  String userId, String date, String time) {
		// 1. 데이터 가공: 문자열 날짜와 시간을 하나의 Date 객체로 병합
		System.out.println("Date : " + date);
		System.out.println("Time : " + time);

		// 1. 중복 체크 (가게 시간)
		int timeDuplicateCount = book_mapper.checkDuplicateTime(storeId, date, time);
		System.out.println("중복 체크 (가게 시간) : " + timeDuplicateCount);
		
		// 중복이면 1, 중복이 아니면 0
		return timeDuplicateCount;
	}
	
	@Override
	public int checkUserDailyBook(int storeId, String userId, String date) {
		
		// 2. 중복 체크 (유저 당일)
		int userDailyCount = book_mapper.checkUserDailyBook(storeId, userId, date);
		System.out.println("중복 체크 (유저 당일) : " + userDailyCount);
		
		// 중복이면 1, 중복이 아니면 0
		return userDailyCount;
	}
//    @Override
//    public void register_book(BookVO vo, String date, String time) {
//    	try {
//    		// 1. 데이터 가공: 문자열 날짜와 시간을 하나의 Date 객체로 병합
//    		String full_date = date + " " + time; 
//    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//    		Date combined_date = sdf.parse(full_date);
//    		vo.setBook_date(combined_date);
//    		
//    		// 2. [검증 A] 동일 시간대 중복 체크 (해당 가게의 해당 시간에 이미 예약이 있는지)
//    		int timeDuplicateCount = book_mapper.checkDuplicateTime(vo.getStore_id(), date, time);
//    		if (timeDuplicateCount > 0) {
//    			// 예외를 던지면 @Transactional에 의해 아래 insert가 실행되지 않고 롤백됩니다.
//    			throw new RuntimeException("죄송합니다. 방금 다른 분의 예약이 완료된 시간대입니다.");
//    		}
//    		
//    		// 3. [검증 B] 유저 당일 중복 체크 (해당 유저가 오늘 해당 가게에 이미 예약했는지)
//    		int userDailyCount = book_mapper.checkUserDailyBook(vo.getStore_id(), vo.getUser_id(), date);
//    		if (userDailyCount > 0) {
//    			throw new RuntimeException("이미 오늘 이 매장에 예약하신 내역이 존재합니다.");
//    		}
//    		
//    		if (is_debug_mode) {
//    			System.out.println("DEBUG: 모든 검증 통과 - 예약 저장을 진행합니다. [" + full_date + "]");
//    		}
//    		
//    		// 4. 모든 검증 통과 시 최종 저장
//    		book_mapper.insertBook(vo);
//    		
//    	} catch (RuntimeException re) {
//    		// 비즈니스 로직 위반 시 던진 메시지를 그대로 전달
//    		throw re;
//    	} catch (Exception e) {
//    		// 날짜 파싱 오류 등 시스템 예외 처리
//    		throw new RuntimeException("예약 처리 중 오류가 발생했습니다: " + e.getMessage());
//    	}
//    }

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

	@Override
	public List<BookVO> get_store_book_list_by_date(int store_id, String book_date) {
	    return book_mapper.selectStoreBookListByDate(store_id, book_date);
	}
}