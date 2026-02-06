/* com/uhi/gourmet/member/MemberController.java */
package com.uhi.gourmet.member;

import java.security.Principal;
import java.util.List;
import java.util.Random;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.pagehelper.PageInfo; // 추가
import com.uhi.gourmet.book.BookService;
import com.uhi.gourmet.wait.WaitService;
import com.uhi.gourmet.store.StoreMapper;
import com.uhi.gourmet.store.StoreVO;
import com.uhi.gourmet.review.ReviewService; 
import com.uhi.gourmet.review.ReviewVO;

import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/member")
@Log4j2
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

    @Autowired
    private JavaMailSenderImpl mailSender;

    @Value("${kakao.js.key}")
    private String kakaoJsKey;

    private void addKakaoKeyToModel(Model model) {
        model.addAttribute("kakaoJsKey", kakaoJsKey);
    }

    @PostMapping("/emailAuth")
    @ResponseBody
    public int emailAuth(@RequestParam("email") String email) {
        log.info("이메일 인증 요청 수신: " + email);
        Random random = new Random();
        int checkNum = random.nextInt(888888) + 111111;

        String setFrom = "boardexample114@gmail.com";
        String toMail = email;
        String title = "Gourmet 회원가입 인증 이메일입니다.";
        String content = "GourmetPass를 이용해주셔서 감사합니다.<br><br>" +
                         "인증 코드는 <b>" + checkNum + "</b> 입니다.<br>" +
                         "해당 인증 코드를 인증 코드 확인란에 기입하여 주세요.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(setFrom);
            helper.setTo(toMail);
            helper.setSubject(title);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("메일 전송 실패: " + e.getMessage());
        }
        return checkNum;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) model.addAttribute("msg", "아이디 또는 비밀번호를 확인해주세요.");
        return "member/login";
    }

    @GetMapping("/signup/select")
    public String signupSelectPage() { return "member/signup_select"; }

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

    /**
     * 마이페이지 메인
     * 일반 사용자의 경우 최근 리뷰 3개만 요약해서 보여주도록 수정
     */
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
                
                // 점주 마이페이지는 최근 리뷰 10개 표시
                model.addAttribute("store_review_list", review_service.getStoreReviews(store.getStore_id(), 1, 10).getList());
            } else {
                model.addAttribute("noStoreMsg", "등록된 매장 정보가 없습니다.");
            }
            return "member/mypage_owner";
        } else {
            // [수정] 일반 사용자: 마이페이지용으로 최근 리뷰 3개만 로드 (PageHelper 활용)
            PageInfo<ReviewVO> reviewPage = review_service.getMyReviewsPaginated(user_id, 1, 3);
            
            model.addAttribute("my_review_list", reviewPage.getList());
            model.addAttribute("total_review_cnt", reviewPage.getTotal()); // UI에서 '전체보기(N)' 표시용
            return "member/mypage"; 
        }
    }

    /**
     * [신규] 내가 쓴 리뷰 이력 전체 보기 (페이징 게시판)
     */
    @GetMapping("/review/mine")
    public String myReviewList(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            Principal principal, Model model) {
        
        if (principal == null) return "redirect:/member/login";
        
        String user_id = principal.getName();
        
        // 한 페이지에 10개씩 출력하도록 설정
        PageInfo<ReviewVO> pageMaker = review_service.getMyReviewsPaginated(user_id, pageNum, 3);
        
        model.addAttribute("allReviews", pageMaker.getList());
        model.addAttribute("pageMaker", pageMaker);
        
        return "review/review_mine"; // 신규 JSP 경로
    }

    @GetMapping("/wait_status")
    public String myStatus(Principal principal, Model model) {
        if (principal == null) return "redirect:/member/login";
        model.addAllAttributes(memberService.getMyStatusSummary(principal.getName()));
        return "wait/wait_status"; 
    }
    
    /**
     * [신규] 전체 이용 내역 페이지
     */
    @GetMapping("/history")
    public String myHistory(Principal principal, Model model) {
        if (principal == null) return "redirect:/member/login";
        model.addAllAttributes(memberService.getMyStatusSummary(principal.getName()));
        return "wait/wait_history"; // 새로운 JSP
    }
    
    
    @GetMapping("/edit")
    public String editPage(Principal principal, Model model) {
        model.addAttribute("member", memberService.getMember(principal.getName()));
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
        if (session != null) session.invalidate();
        rttr.addFlashAttribute("msg", "정상적으로 탈퇴되었습니다.");
        return "redirect:/";
    }

    @PostMapping("/idCheck")
    @ResponseBody
    public String idCheck(@RequestParam("user_id") String user_id) {
        return (memberService.checkIdDuplicate(user_id) > 0) ? "fail" : "success";
    }
}