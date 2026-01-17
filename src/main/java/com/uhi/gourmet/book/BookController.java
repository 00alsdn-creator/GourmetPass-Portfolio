/* com/uhi/gourmet/book/BookController.java */
package com.uhi.gourmet.book;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    // 1. 점주용 실시간 관리 페이지 로드 로직
    @GetMapping("/manage")
    public String manage_page(Principal principal, Model model) {
        String user_id = principal.getName();
        StoreVO store = store_mapper.getStoreByUserId(user_id);
        
        if (store != null) {
            int store_id = store.getStore_id();
            model.addAttribute("store_book_list", book_service.get_store_book_list(store_id));
            model.addAttribute("store_wait_list", wait_service.get_store_wait_list(store_id));
            model.addAttribute("store", store);
        }
        return "book/manage";
    }

    // 2. 예약 등록 프로세스 (리팩토링: 날짜 처리 로직 서비스 위임)
    @PostMapping("/register")
    public String register_book(BookVO vo, Principal principal,
                                @RequestParam("store_id") int store_id,
                                @RequestParam("book_date") String date,
                                @RequestParam("book_time") String time,
                                @RequestParam(value="people_cnt", defaultValue="1") int people_cnt) {
        
        if (principal == null) return "redirect:/member/login";

        try {
            // [수정 포인트] 컨트롤러의 책임을 파라미터 수집 및 VO 세팅으로 한정
            vo.setUser_id(principal.getName());
            vo.setStore_id(store_id);
            vo.setPeople_cnt(people_cnt);

            // [수정 포인트] 날짜 파싱 및 병합(SimpleDateFormat) 로직을 서비스로 이전
            book_service.register_book(vo, date, time);
            
        } catch (Exception e) {
            // 서비스에서 던진 RuntimeException 등을 캐치하여 에러 처리
            System.err.println("!!! [BOOK ERROR] : " + e.getMessage());
            return "redirect:/store/detail?store_id=" + store_id;
        }
        
        return "redirect:/member/mypage";
    }

    // 3. 예약 상태 업데이트 처리 (방문/노쇼)
    @PostMapping("/updateStatus")
    public String update_status(@RequestParam("book_id") int book_id, 
                                @RequestParam("status") String status) {
        book_service.update_book_status(book_id, status);
        return "redirect:/book/manage";
    }
}