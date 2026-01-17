/* com/uhi/gourmet/member/MemberController.java */
package com.uhi.gourmet.member;

import java.security.Principal;
import java.util.ArrayList; // 추가
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.uhi.gourmet.book.BookService;
import com.uhi.gourmet.book.BookVO;
import com.uhi.gourmet.wait.WaitService;
import com.uhi.gourmet.wait.WaitVO;
import com.uhi.gourmet.store.StoreMapper;
import com.uhi.gourmet.store.StoreVO;
import com.uhi.gourmet.review.ReviewService; 
import com.uhi.gourmet.review.ReviewVO;      

@Controller
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService; 

    @Autowired
    private StoreMapper storeMapper; 

    @Autowired
    private BookService book_service;

    @Autowired
    private WaitService wait_service;

    @Autowired
    private ReviewService review_service; 

    @Value("${kakao.js.key}")
    private String kakaoJsKey;

    private void addKakaoKeyToModel(Model model) {
        model.addAttribute("kakaoJsKey", kakaoJsKey);
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("msg", "아이디 또는 비밀번호를 확인해주세요.");
        }
        return "member/login";
    }

    @GetMapping("/signup/select")
    public String signupSelectPage() {
        return "member/signup_select";
    }

    @GetMapping("/signup/general")
    public String signupGeneralPage(Model model) {
        addKakaoKeyToModel(model);
        return "member/signup_general"; 
    }

    @PostMapping("/joinProcess")
    public String joinGeneralProcess(MemberVO vo, RedirectAttributes rttr) {
        memberService.joinMember(vo); 
        rttr.addFlashAttribute("msg", "회원가입이 완료되었습니다. 로그인해주세요.");
        return "redirect:/member/login";
    }

    @GetMapping("/signup/owner1")
    public String signupOwner1Page(Model model) {
        addKakaoKeyToModel(model);
        return "member/signup_owner1"; 
    }
    
    @PostMapping("/signup/ownerStep1")
    public String signupOwner1Process(MemberVO member, HttpSession session) {
        session.setAttribute("tempMember", member);
        return "redirect:/member/signup/owner2";
    }

    @GetMapping("/signup/owner2")
    public String signupOwner2Page(Model model) {
        addKakaoKeyToModel(model);
        return "member/signup_owner2";
    }

    @PostMapping("/signup/ownerFinal") 
    public String joinOwnerProcess(StoreVO store, HttpSession session, RedirectAttributes rttr) {
        MemberVO member = (MemberVO) session.getAttribute("tempMember");
        if (member != null) {
            memberService.joinOwner(member, store);
            session.removeAttribute("tempMember");
            rttr.addFlashAttribute("msg", "점주 가입 신청이 완료되었습니다.");
        }
        return "redirect:/member/login";
    }

    @GetMapping("/mypage")
    public String mypage(Principal principal, Model model, HttpServletRequest request) {
        String user_id = principal.getName();
        MemberVO member = memberService.getMember(user_id);
        model.addAttribute("member", member);

        if (request.isUserInRole("ROLE_OWNER")) {
            StoreVO store = storeMapper.getStoreByUserId(user_id);
            if (store != null) {
                model.addAttribute("store", store);
                model.addAttribute("menuList", storeMapper.getMenuList(store.getStore_id()));
                model.addAttribute("store_book_list", book_service.get_store_book_list(store.getStore_id()));
                model.addAttribute("store_review_list", review_service.getStoreReviews(store.getStore_id()));
            } else {
                model.addAttribute("noStoreMsg", "등록된 매장 정보가 없습니다.");
            }
            return "member/mypage_owner";
        } else {
            List<ReviewVO> my_review_list = review_service.getMyReviews(user_id);
            model.addAttribute("my_review_list", my_review_list);
            return "member/mypage"; 
        }
    }

    // [수정 포인트: 로직을 서비스로 위임하여 코드 슬림화]
    @GetMapping("/myStatus")
    public String myStatus(Principal principal, Model model) {
        if (principal == null) return "redirect:/member/login";
        
        String user_id = principal.getName();
        
        // 모든 복잡한 필터링 및 통계 계산은 Service Layer에서 수행
        model.addAllAttributes(memberService.getMyStatusSummary(user_id));
        
        return "member/myStatus"; 
    }

    @GetMapping("/edit")
    public String editPage(Principal principal, Model model) {
        String userId = principal.getName();
        MemberVO member = memberService.getMember(userId);
        model.addAttribute("member", member);
        addKakaoKeyToModel(model);
        return "member/member_edit";
    }
    
    @PostMapping("/edit")
    public String updateProcess(MemberVO vo, RedirectAttributes rttr) {
        memberService.updateMember(vo);
        rttr.addFlashAttribute("msg", "회원 정보가 수정되었습니다.");
        return "redirect:/member/mypage";
    }

    @PostMapping("/delete")
    public String deleteMember(@RequestParam("user_id") String user_id, HttpSession session, RedirectAttributes rttr) {
        memberService.deleteMember(user_id);
        SecurityContextHolder.clearContext();
        if (session != null) {
            session.invalidate();
        }
        rttr.addFlashAttribute("msg", "정상적으로 탈퇴되었습니다.");
        return "redirect:/";
    }

    @PostMapping("/idCheck")
    @ResponseBody
    public String idCheck(@RequestParam("user_id") String user_id) {
        int count = memberService.checkIdDuplicate(user_id);
        return (count > 0) ? "fail" : "success";
    }
}