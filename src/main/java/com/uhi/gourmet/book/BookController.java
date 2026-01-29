/* com/uhi/gourmet/book/BookController.java */
package com.uhi.gourmet.book;

import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uhi.gourmet.store.StoreMapper;
import com.uhi.gourmet.store.StoreVO;
import com.uhi.gourmet.wait.WaitService;

@Controller
@RequestMapping("/book")
public class BookController {

	@Autowired
	private BookService book_service;

	@Autowired
	private WaitService wait_service;

	@Autowired
	private StoreMapper store_mapper;

	/**
	 * [1] 점주용 실시간 매장 관리 센터 [404 해결] 리턴 경로를 실제 파일 위치인 "book/manage"로 수정했습니다.
	 */
	@GetMapping("/manage")
	public String manage_page(Principal principal, Model model) {
		if (principal == null)
			return "redirect:/member/login";

		String user_id = principal.getName();
		StoreVO store = store_mapper.getStoreByUserId(user_id);

		if (store != null) {
			int store_id = store.getStore_id();
			model.addAttribute("store_book_list", book_service.get_store_book_list(store_id));
			model.addAttribute("store_wait_list", wait_service.get_store_wait_list(store_id));
			model.addAttribute("store", store);
		}

		// DispatcherServlet이 /WEB-INF/views/book/manage.jsp를 찾도록 경로 고정
		return "book/manage";
	}

	/**
	 * [2] 예약 등록 프로세스 [교정] 예약 완료 시 웨이팅 현황(myStatus)이 아닌 마이페이지(mypage)로 리다이렉트합니다.
	 */
//    @PostMapping("/register")
//    public String register_book(BookVO vo, Principal principal,
//                                @RequestParam("store_id") int store_id,
//                                @RequestParam("book_date") String date,
//                                @RequestParam("book_time") String time,
//                                @RequestParam(value="people_cnt", defaultValue="1") int people_cnt,
//                                @RequestParam(value="book_price", required=false, defaultValue="0") int book_price) {
//        
//        if (principal == null) return "redirect:/member/login";
//
//        try {
//            // DDL 기반 VO 데이터 세팅
//            vo.setUser_id(principal.getName());
//            vo.setStore_id(store_id);
//            vo.setPeople_cnt(people_cnt);
//            vo.setBook_price(book_price);
//
//            // 날짜(YYYY-MM-DD)와 시간(HH:mm)을 결합하여 TIMESTAMP로 파싱하는 로직은 서비스에서 수행
//            book_service.register_book(vo, date, time);
//            
//            // 예약은 줄서기가 아니므로 마이페이지 내 예약 섹션으로 안내하는 것이 정석입니다.
//            return "redirect:/member/mypage";
//            
//        } catch (Exception e) {
//        	// 등록 과정에서 오류가 나면 store_detail 페이지로 다시 보냄
//            System.err.println("!!! [BOOK REGISTER ERROR] : " + e.getMessage());
//            return "redirect:/store/detail?storeId=" + store_id;
//        }
//    }
	@GetMapping("/api/checkDuplicate")
	@ResponseBody
	public String checkDuplicate(@RequestParam("store_id") int storeId, 
	                             @RequestParam("book_date") String bookDate,
	                             @RequestParam("book_time") String bookTime,
	                             Principal principal) {
		System.out.println("중복체크 Controller 시작...");
		
	    if (principal == null) return "LOGIN_REQUIRED";
	    
	    //userId추출을 위해
	    String userId = principal.getName();
	    
		System.out.println("서비스의 기존 중복 체크 시작...");
	    // 서비스의 기존 중복 체크 로직 재사용
	    int timeDup = book_service.checkDuplicateTime(storeId, userId, bookDate, bookTime);
	    if (timeDup > 0) return "DUPLICATE_TIME";	// 예약 중복이면 실행
	    
		System.out.println("같은 일(day)에 이미 예약 체크 시작...");
	    int userDup = book_service.checkUserDailyBook(storeId, userId, bookDate);
	    if (userDup > 0) return "DUPLICATE_USER";	// 같은 일(day)에 이미 예약이 있으면 실행
	    
	    return "AVAILABLE";
	}
	
	@PostMapping("/register")
	public String register_book(Principal principal, @RequestParam("store_id") int store_id,
			@RequestParam("book_date") String date, @RequestParam("book_time") String time,
			@RequestParam("pay_id") String pay_id,
			@RequestParam(value = "people_cnt", defaultValue = "1") int people_cnt,
			@RequestParam(value = "book_price", required = false, defaultValue = "0") int book_price,
			RedirectAttributes rttr) {
		System.out.println("Book register Controller...");
		
		if (principal == null) return "redirect:/member/login";
		
		BookVO vo = new BookVO();
	    // 1. 여기서 문자열일 때 미리 합쳐서 Date 객체로 만듭니다.
	    try {
	        String fullDateStr = date + " " + time; // "2026-01-21 17:00"
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	        Date combinedDate = sdf.parse(fullDateStr);
	        vo.setBook_date(combinedDate); // VO에 시/분까지 포함된 날짜 세팅
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }

	    vo.setUser_id(principal.getName());
	    vo.setStore_id(store_id);
	    vo.setPeople_cnt(people_cnt);
	    vo.setBook_time(time);
	    vo.setPay_id(Integer.parseInt(pay_id));
	    
	    book_service.register_book(vo);
	    rttr.addFlashAttribute("msg", "예약이 완료되었습니다.");
	    return "redirect:/member/mypage";
	}
	
	
//	@PostMapping("/register")
//	public String register_book(BookVO vo, Principal principal, @RequestParam("store_id") int store_id,
//			@RequestParam("book_date") String date, @RequestParam("book_time") String time,
//			@RequestParam(value = "people_cnt", defaultValue = "1") int people_cnt,
//			@RequestParam(value = "book_price", required = false, defaultValue = "0") int book_price,
//			RedirectAttributes rttr) {
//		
//		if (principal == null)
//			return "redirect:/member/login";
//		
//		// DDL 기반 VO 데이터 세팅
//		vo.setUser_id(principal.getName());
//		vo.setStore_id(store_id);
//		vo.setPeople_cnt(people_cnt);
//		vo.setBook_price(book_price);
//		
//		String result = book_service.register_book(vo, date, time);
//		System.out.println(result);
//		
//		
//		// SUCCESS가 아닐 때 리다이렉트 부분
//		if ("SUCCESS".equals(result)) {
//			rttr.addFlashAttribute("msg", "예약이 완료되었습니다.");
//			return "redirect:/member/mypage";
//		} else {
//			// 중복 결과에 따른 메시지 설정
//			String errorMsg = "";
//			if ("DUPLICATE_TIME".equals(result)) {
//				errorMsg = "죄송합니다. 이미 예약이 완료된 시간대입니다.";
//			} else if ("DUPLICATE_USER".equals(result)) {
//				errorMsg = "해당 매장에는 동일한 날에 다중예약이 불가능합니다.";
//			} else {
//				errorMsg = "예약 중 오류가 발생했습니다.";
//			}
//			
//			rttr.addFlashAttribute("msg", errorMsg);
//			// 다시 예약 페이지(상세페이지)로 이동
//			return "redirect:/store/detail?storeId=" + store_id;
//		}
//	}

	/**
	 * [3] 예약 상태 업데이트 (입장/노쇼)
	 */
//    @PostMapping("/updateStatus")
//    public String update_status(@RequestParam("book_id") int book_id, 
//                                @RequestParam("status") String status) {
//        book_service.update_book_status(book_id, status);
//        
//        // 상태 변경 후 다시 점주용 관리 페이지로 복귀
//        return "redirect:/book/manage";
//    }

	// 예약 상태 제어 (예약확정 -> 입장확인 -> 식사완료), 노쇼, 사용자의 예약 취소(Cancel)
	@PostMapping("/updateStatus")
	public String updateBookStatus(@RequestParam("book_id") int bookId, @RequestParam("status") String status,
			@RequestParam(value = "user_id", required = false) String userId, Authentication auth) {// 권한 확인을 위해
																									// Authentication 추가
		System.out.println("Status : " + status);

		// 현재 로그인한 사용자의 권한을 확인합니다.
		boolean isOwner = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

		book_service.update_book_status(bookId, status);

//        if (userId != null && !userId.isEmpty()) {
//            String msg = "예약 상태가 [" + status + "]로 변경되었습니다.";
//            messagingTemplate.convertAndSend("/topic/wait/" + userId, msg);
//        }

		if (isOwner) {
			// 점주라면 원래대로 매장 관리 페이지로
			return "redirect:/book/manage";
		} else {
			// 일반 사용자라면 마이페이지(이용 현황)로
			return "redirect:/member/wait_status"; // 또는 /member/mypage
		}
	}

}