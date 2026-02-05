/* com/uhi/gourmet/store/StoreController.java */
package com.uhi.gourmet.store;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;
import com.uhi.gourmet.book.BookService;
import com.uhi.gourmet.member.MemberService;
import com.uhi.gourmet.member.MemberVO;
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
    private MemberService memberService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${kakao.js.key}")
    private String kakaoJsKey;
    
    /* portOne 관련 값 */
    @Value("${portone.store.id}")
    private String portOneStoreId;
    @Value("${portone.channel.key}")
    private String portOneChannelKey;

    /**
     * [수정] 1. 맛집 목록 조회 (PageHelper 기반 페이징 적용)
     * Criteria 객체 대신 @RequestParam을 사용하여 페이지 번호와 검색 필터를 직접 받습니다.
     */
    @GetMapping("/list")
    public String storeList(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "6") int pageSize,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
        
        // [로직 1] 서비스에서 PageHelper.startPage()를 호출하고 결과를 PageInfo 객체로 받습니다.
        PageInfo<StoreVO> pageMaker = storeService.getStoreList(pageNum, pageSize, category, region, keyword);
        
        // [로직 2] 실제 데이터 리스트는 PageInfo의 getList()를 통해 꺼냅니다.
        model.addAttribute("storeList", pageMaker.getList()); 
        
        // [로직 3] PageDTO의 역할을 PageInfo가 완전히 대체합니다.
        // JSP에서는 기존의 pageMaker.startPage 등과 유사한 PageInfo 속성을 사용하게 됩니다.
        model.addAttribute("pageMaker", pageMaker);
        
        // [상태 유지] 필터 선택 정보 유지
        model.addAttribute("category", category);
        model.addAttribute("region", region);
        model.addAttribute("keyword", keyword);
        
        return "store/store_list";
    }

    // 2. 맛집 상세 정보 조회 (최근 리뷰 3개 요약 포함)
    @GetMapping("/detail")
    public String storeDetail(@RequestParam("storeId") int storeId, Model model, Principal principal) {
        
        if (principal != null) {
            MemberVO loginUser = memberService.getMember(principal.getName()); 
            model.addAttribute("loginUser", loginUser);
        }
        
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
        }

        List<ReviewVO> reviewList = reviewService.getStoreReviews(storeId);
        if (reviewList != null && reviewList.size() > 3) {
            reviewList = reviewList.subList(0, 3);
        }
        
        model.addAttribute("store", store);
        model.addAttribute("menuList", menuList);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("portOneStoreId", portOneStoreId);
        model.addAttribute("portOneChannelKey", portOneChannelKey);

        boolean canWriteReview = (principal != null) && reviewService.checkReviewEligibility(principal.getName(), storeId);
        model.addAttribute("canWriteReview", canWriteReview);
        
        return "store/store_detail";
    }
    
    // 3. 전체 리뷰 게시판 조회
    @GetMapping("/reviews")
    public String allReviews(@RequestParam("store_id") int storeId, Model model) {
        StoreVO store = storeService.getStoreDetail(storeId);
        List<ReviewVO> allReviews = reviewService.getStoreReviews(storeId);
        
        model.addAttribute("store", store);
        model.addAttribute("allReviews", allReviews);
        
        return "store/store_reviews";
    }
    
    // 4. API: 예약 가능 시간 슬롯 동적 조회
    @GetMapping(value = "/api/timeSlots", produces = "application/json; charset=UTF-8")
    @ResponseBody 
    public List<String> getTimeSlots(@RequestParam("store_id") int storeId, 
                                   @RequestParam("book_date") String bookDate) {
        StoreVO store = storeService.getStoreDetail(storeId);
        return storeService.getAvailableTimeSlots(store, bookDate);
    }

    // ================= [실시간 매장 관리: 점주 전용] =================

    @PostMapping("/wait/updateStatus")
    public String updateWaitStatus(@RequestParam("wait_id") int waitId, 
                                   @RequestParam("status") String status,
                                   @RequestParam("user_id") String userId) {

        waitService.update_wait_status(waitId, status);
        messagingTemplate.convertAndSend("/topic/wait/" + userId, status);
        
        return "redirect:/book/manage"; 
    }

    // ================= [가게 및 메뉴 정보 관리] =================

    @GetMapping("/register")
    public String registerStorePage() {
        return "store/store_register";
    }

    @PostMapping("/register")
    public String registerStoreProcess(@ModelAttribute StoreVO vo, 
                                     @RequestParam(value="file", required=false) MultipartFile file,
                                     HttpServletRequest request, Principal principal) {
        if (file != null && !file.isEmpty()) {
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
                                     HttpServletRequest request, Principal principal) {
        if (file != null && !file.isEmpty()) {
            String realPath = request.getSession().getServletContext().getRealPath("/resources/upload");
            vo.setStore_img(storeService.uploadFile(file, realPath));
        }
        storeService.modifyStore(vo, principal.getName());
        return "redirect:/member/mypage";
    }

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
                                      HttpServletRequest request, Principal principal) {
        if (file != null && !file.isEmpty()) {
            String realPath = request.getSession().getServletContext().getRealPath("/resources/upload");
            menuVO.setMenu_img(storeService.uploadFile(file, realPath));
        }
        storeService.addMenu(menuVO, principal.getName());
        return "redirect:/member/mypage"; 
    }

    @PostMapping("/menu/delete")
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
                                    HttpServletRequest request, Principal principal) {
        if (file != null && !file.isEmpty()) {
            String realPath = request.getSession().getServletContext().getRealPath("/resources/upload");
            vo.setMenu_img(storeService.uploadFile(file, realPath));
        }
        storeService.modifyMenu(vo, principal.getName());
        return "redirect:/member/mypage";
    }
}