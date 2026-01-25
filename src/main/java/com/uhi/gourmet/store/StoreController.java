/* com/uhi/gourmet/store/StoreController.java */
package com.uhi.gourmet.store;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate; // 웹소켓 메시지 전송용
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

    // 실시간 알림 신호를 보내기 위한 템플릿 주입
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${kakao.js.key}")
    private String kakaoJsKey;
    
	//portOne 결제 시작을 위한 변수
    @Value("${portone.imp.init}")
    private String impInit;
    @Value("${portone.pg}")
    private String pg;	// 테스트 결제를 위한 변수
    
    

    // 1. 맛집 목록 조회 (카테고리, 지역, 검색어 필터링)
    @GetMapping("/list")
    public String storeList(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
    	
    	System.out.println(category);
    	System.out.println(region);
    	System.out.println(keyword);
        
        List<StoreVO> storeList = storeService.getStoreList(category, region, keyword);
        model.addAttribute("storeList", storeList); 
        model.addAttribute("category", category);
        model.addAttribute("region", region);
        model.addAttribute("keyword", keyword);
        
        return "store/store_list";
    }

    // 2. 맛집 상세 정보 조회 (최근 리뷰 3개 요약 포함)
    @GetMapping("/detail")
    public String storeDetail(@RequestParam("storeId") int storeId, Model model, Principal principal) {
        
    	// 결제를 위한 JSP에 멤버정보 넣기
    	if (principal != null) {
            // principal.getName()은 현재 로그인한 사용자의 ID를 반환합니다.
            // memberService에 ID로 회원 객체를 가져오는 메서드가 있다고 가정합니다.
    		
    		System.out.println("ID : " + principal.getName());
            MemberVO loginUser = memberService.getMember(principal.getName()); 
            System.out.println(loginUser);
            
            
            // JSP에서 사용할 수 있도록 "loginUser"라는 이름으로 전달
            model.addAttribute("loginUser", loginUser);
        }
    	
    	storeService.plusViewCount(storeId);	// 조회수 증가
        
        StoreVO store = storeService.getStoreDetail(storeId);
        List<MenuVO> menuList = storeService.getMenuList(storeId);
        
        // 실시간 대기 팀 수 조회 (관리자 대시보드와 동기화)
        int currentWaitCount = waitService.get_current_wait_count(storeId);
        model.addAttribute("currentWaitCount", currentWaitCount);

        Map<String, Object> stats = reviewService.getReviewStats(storeId);
        if (store != null && stats != null) {
            Object cntVal = stats.get("review_cnt");
            Object rateVal = stats.get("avg_rating");

            store.setReview_cnt(cntVal != null ? Integer.parseInt(String.valueOf(cntVal)) : 0);
            store.setAvg_rating(rateVal != null ? Double.parseDouble(String.valueOf(rateVal)) : 0.0);
        }

        // 상세페이지 전용: 최근 리뷰 최대 3개까지만 추출
        List<ReviewVO> reviewList = reviewService.getStoreReviews(storeId);
        if (reviewList != null && reviewList.size() > 3) {
            reviewList = reviewList.subList(0, 3);
        }
        
        model.addAttribute("store", store);
        model.addAttribute("menuList", menuList);
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        model.addAttribute("impInit", impInit);	// portone 결제를 위한 변수
        model.addAttribute("pg", pg);	// portone 결제를 위한 변수

        // 리뷰 작성 자격 체크
        boolean canWriteReview = (principal != null) && reviewService.checkReviewEligibility(principal.getName(), storeId);
        model.addAttribute("canWriteReview", canWriteReview);
        
        return "store/store_detail";
    }
    
    // 3. 전체 리뷰 게시판 조회 (일반 회원 접근 허용 경로)
    @GetMapping("/reviews")
    public String allReviews(@RequestParam("store_id") int storeId, Model model) {
        StoreVO store = storeService.getStoreDetail(storeId);
        List<ReviewVO> allReviews = reviewService.getStoreReviews(storeId);
        
        model.addAttribute("store", store);
        model.addAttribute("allReviews", allReviews);
        
        return "store/store_reviews";
    }
    
    /**
     * [리팩토링] 4. API: 예약 가능 시간 슬롯 동적 조회
     * 특정 날짜를 기준으로 이미 예약된 슬롯을 제외하고 반환합니다. (중복 예약 방지)
     */
    @GetMapping(value = "/api/timeSlots", produces = "application/json; charset=UTF-8")
    @ResponseBody 
    public List<String> getTimeSlots(@RequestParam("store_id") int storeId, 
                                   @RequestParam("book_date") String bookDate) {
        StoreVO store = storeService.getStoreDetail(storeId);
        // DB에서 해당 날짜의 예약 현황을 체크하여 남은 시간만 생성하는 서비스 메서드 호출
        return storeService.getAvailableTimeSlots(store, bookDate);
    }

    // ================= [실시간 매장 관리: 점주 전용] =================

    // 웨이팅 상태 제어 (호출 -> 입장확인 -> 식사완료)
    @PostMapping("/wait/updateStatus")
    public String updateWaitStatus(@RequestParam("wait_id") int waitId, 
                                   @RequestParam("status") String status,
                                   @RequestParam("user_id") String userId) {

        waitService.update_wait_status(waitId, status);
        
        // [핵심] 해당 유저의 개인 채널로 "상태 변경됨" 신호를 보냅니다.
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