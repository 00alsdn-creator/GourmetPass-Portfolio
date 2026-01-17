/* com/uhi/gourmet/store/StoreController.java */
package com.uhi.gourmet.store;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.uhi.gourmet.book.BookService;
import com.uhi.gourmet.review.ReviewService;
import com.uhi.gourmet.review.ReviewVO;
import com.uhi.gourmet.wait.WaitService;

@Controller
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private WaitService waitService;

    @Autowired
    private BookService bookService;

    @Autowired
    private ReviewService reviewService;

    @Value("${kakao.js.key}")
    private String kakaoJsKey;

    // 1. 맛집 목록 조회
    @GetMapping("/list")
    public String storeList(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
        
        List<StoreVO> storeList = storeService.getStoreList(category, region, keyword);
        model.addAttribute("storeList", storeList); 
        model.addAttribute("category", category);
        model.addAttribute("region", region);
        model.addAttribute("keyword", keyword);
        
        return "store/store_list";
    }

    // 2. 맛집 상세 정보 조회 (리팩토링: 시간 슬롯 생성 로직을 서비스로 위임)
    @GetMapping("/detail")
    public String storeDetail(@RequestParam("storeId") int storeId, Model model, Principal principal) {
        storeService.plusViewCount(storeId);
        
        StoreVO store = storeService.getStoreDetail(storeId);
        List<MenuVO> menuList = storeService.getMenuList(storeId);
        
        int currentWaitCount = waitService.get_current_wait_count(storeId);
        model.addAttribute("currentWaitCount", currentWaitCount);

        Map<String, Object> stats = reviewService.getReviewStats(storeId);
        if (store != null && stats != null) {
            Object cntVal = stats.get("review_cnt");
            Object rateVal = stats.get("avg_rating");

            store.setReview_cnt(cntVal != null ? Integer.parseInt(String.valueOf(cntVal)) : 0);
            store.setAvg_rating(rateVal != null ? Double.parseDouble(String.valueOf(rateVal)) : 0.0);
            
            // [수정 포인트] 서비스 계층에서 시간 슬롯 데이터 생성
            model.addAttribute("timeSlots", storeService.generateTimeSlots(store));
        }

        List<ReviewVO> reviewList = reviewService.getStoreReviews(storeId);
        model.addAttribute("reviewList", reviewList);

        boolean canWriteReview = false;
        if (principal != null) {
            canWriteReview = reviewService.checkReviewEligibility(principal.getName(), storeId);
        }
        model.addAttribute("canWriteReview", canWriteReview);

        model.addAttribute("store", store);
        model.addAttribute("menuList", menuList);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        
        return "store/store_detail";
    }
    
    @GetMapping(value = "/api/timeSlots", produces = "application/json; charset=UTF-8")
    @ResponseBody 
    public List<String> getTimeSlots(@RequestParam("store_id") int storeId) {
        StoreVO store = storeService.getStoreDetail(storeId);
        // [수정 포인트] 내부 private 메서드 대신 서비스 메서드 호출
        return storeService.generateTimeSlots(store);
    }

    // ================= [점주 전용: 상태 제어 로직] =================

    @PostMapping("/wait/updateStatus")
    public String updateWaitStatus(@RequestParam("wait_id") int waitId, 
                                 @RequestParam("status") String status) {
        waitService.update_wait_status(waitId, status);
        return "redirect:/member/mypage";
    }

    @PostMapping("/book/updateStatus")
    public String updateBookStatus(@RequestParam("book_id") int bookId, 
                                 @RequestParam("status") String status) {
        bookService.update_book_status(bookId, status);
        return "redirect:/member/mypage";
    }

    // ================= [가게 정보 관리 (리팩토링: 파일 업로드 로직 서비스 위임)] =================

    @GetMapping("/register")
    public String registerStorePage() {
        return "store/store_register";
    }

    @PostMapping("/register")
    public String registerStoreProcess(@ModelAttribute StoreVO vo, 
                                     @RequestParam(value="file", required=false) MultipartFile file,
                                     HttpServletRequest request,
                                     Principal principal) {
        if (file != null && !file.isEmpty()) {
            // [수정 포인트] 서비스 계층으로 실제 저장 경로 전달 후 처리
            String realPath = request.getSession().getServletContext().getRealPath("/resources/upload");
            vo.setStore_img(storeService.uploadFile(file, realPath));
        }
        storeService.registerStore(vo, principal.getName());
        return "redirect:/member/mypage";
    }

    @GetMapping("/update")
    public String updateStorePage(@RequestParam("store_id") int storeId, Model model, Principal principal) {
        StoreVO store = storeService.getMyStore(storeId, principal.getName());
        if (store == null) return "redirect:/member/mypage";
        
        model.addAttribute("store", store);
        return "store/store_update";
    }

    @PostMapping("/update")
    public String updateStoreProcess(@ModelAttribute StoreVO vo, 
                                     @RequestParam(value="file", required=false) MultipartFile file, 
                                     HttpServletRequest request,
                                     Principal principal) {
        if (file != null && !file.isEmpty()) {
            String realPath = request.getSession().getServletContext().getRealPath("/resources/upload");
            vo.setStore_img(storeService.uploadFile(file, realPath));
        }
        storeService.modifyStore(vo, principal.getName());
        return "redirect:/member/mypage";
    }

    // ================= [메뉴 관리] =================

    @GetMapping("/menu/register")
    public String menuRegisterPage(@RequestParam("store_id") int storeId, Model model, Principal principal) {
        StoreVO store = storeService.getMyStore(storeId, principal.getName());
        if (store == null) return "redirect:/member/mypage";
        
        model.addAttribute("store_id", storeId);
        return "store/menu_register";
    }

    @PostMapping("/menu/register")
    public String menuRegisterProcess(@ModelAttribute MenuVO menuVO, 
                                      @RequestParam(value="file", required=false) MultipartFile file,
                                      HttpServletRequest request,
                                      Principal principal) {
        if (file != null && !file.isEmpty()) {
            String realPath = request.getSession().getServletContext().getRealPath("/resources/upload");
            menuVO.setMenu_img(storeService.uploadFile(file, realPath));
        }
        storeService.addMenu(menuVO, principal.getName());
        return "redirect:/member/mypage"; 
    }

    @GetMapping("/menu/delete")
    public String deleteMenu(@RequestParam("menu_id") int menuId, Principal principal) {
        storeService.removeMenu(menuId, principal.getName());
        return "redirect:/member/mypage";
    }
    
    @GetMapping("/menu/update")
    public String menuUpdatePage(@RequestParam("menu_id") int menuId, Model model, Principal principal) {
        MenuVO menu = storeService.getMenuDetail(menuId, principal.getName());
        if (menu == null) return "redirect:/member/mypage";
        
        model.addAttribute("menu", menu);
        return "store/menu_update";
    }
    
    @PostMapping("/menu/update")
    public String menuUpdateProcess(@ModelAttribute MenuVO vo, 
                                    @RequestParam(value="file", required=false) MultipartFile file,
                                    HttpServletRequest request,
                                    Principal principal) {
        if (file != null && !file.isEmpty()) {
            String realPath = request.getSession().getServletContext().getRealPath("/resources/upload");
            vo.setMenu_img(storeService.uploadFile(file, realPath));
        }
        storeService.modifyMenu(vo, principal.getName());
        return "redirect:/member/mypage";
    }
}