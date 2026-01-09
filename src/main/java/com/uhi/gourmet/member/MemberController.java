package com.uhi.gourmet.member;

import java.security.Principal;
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

import com.uhi.gourmet.member.MemberService; // Service 인터페이스 import
import com.uhi.gourmet.store.StoreMapper;
import com.uhi.gourmet.store.StoreVO;

@Controller
@RequestMapping("/member") 
public class MemberController {

    @Autowired
    private MemberService memberService; // 변경: Mapper -> Service 사용

    @Autowired
    private StoreMapper storeMapper; // 조회용으로 유지 (필요 시 StoreService로 분리 가능)

    // 암호화 인코더(BCryptPasswordEncoder)는 Service 내부로 이동했으므로 제거

    @Value("${kakao.js.key}")
    private String kakaoJsKey;

    private void addKakaoKeyToModel(Model model) {
        model.addAttribute("kakaoJsKey", kakaoJsKey);
    }

    // ================= [로그인 & 로그아웃] =================
    
    @GetMapping("/login") 
    public String loginPage() {
        return "member/login";
    }
    
    // ================= [회원가입] =================

    @GetMapping("/signup/select") 
    public String joinSelect() {
        return "member/signup_select";
    }

    @GetMapping("/signup/general") 
    public String joinGeneralPage(Model model) {
        addKakaoKeyToModel(model);
        return "member/signup_general";
    }

    @PostMapping("/joinProcess")
    public String joinProcess(MemberVO vo) {
        // 로직 이임: 암호화 및 권한 설정은 Service가 담당
        memberService.joinMember(vo);
        return "redirect:/member/login"; 
    }

    @GetMapping("/signup/owner1") 
    public String ownerStep1(Model model) {
        addKakaoKeyToModel(model);
        return "member/signup_owner1";
    }

    @PostMapping("/signup/ownerStep1")
    public String ownerStep1Process(MemberVO memberVo, HttpSession session) {
        session.setAttribute("tempMember", memberVo);
        return "redirect:/member/signup/owner2";
    }

    @GetMapping("/signup/owner2")
    public String ownerStep2(HttpSession session, Model model) {
        if (session.getAttribute("tempMember") == null) {
            return "redirect:/member/signup/owner1";
        }
        addKakaoKeyToModel(model);
        return "member/signup_owner2";
    }

    @PostMapping("/signup/ownerFinal")
    public String ownerFinalProcess(StoreVO storeVo, HttpSession session) {
        MemberVO memberVo = (MemberVO) session.getAttribute("tempMember");
        if (memberVo == null) return "redirect:/member/signup/owner1";

        // 로직 이임: 트랜잭션(@Transactional) 처리도 Service 내부에서 수행
        memberService.joinOwner(memberVo, storeVo);

        session.removeAttribute("tempMember");
        return "redirect:/member/login";
    }

    // ================= [마이페이지 & 수정 & 탈퇴] =================

    @GetMapping("/mypage")
    public String mypage(Principal principal, Model model, HttpServletRequest request) {
        String userId = principal.getName();
        
        // 1. 회원 기본 정보 로드 (Service 사용)
        MemberVO member = memberService.getMember(userId);
        model.addAttribute("member", member);

        // 2. 점주 권한인 경우 (1:1 구조 반영)
        // Store 조회 로직은 일단 기존 Mapper 유지 (추후 StoreService 생성 권장)
        if (request.isUserInRole("ROLE_OWNER")) {
            StoreVO store = storeMapper.getStoreByUserId(userId);
            model.addAttribute("store", store); 
            
            if (store != null) {
                model.addAttribute("menuList", storeMapper.getMenuList(store.getStore_id()));
            }
            
            return "member/mypage_owner";
        }
        
        return "member/mypage";
    }

    @GetMapping("/edit")
    public String editPage(Principal principal, Model model) {
        String userId = principal.getName();
        // Service 사용
        MemberVO member = memberService.getMember(userId);
        model.addAttribute("member", member);
        addKakaoKeyToModel(model);
        return "member/member_edit"; 
    }
    
    @PostMapping("/edit")
    public String updateProcess(MemberVO vo, RedirectAttributes rttr) {
        // 로직 이임: 비밀번호 암호화 여부 판단은 Service가 담당
        memberService.updateMember(vo);
        rttr.addFlashAttribute("msg", "회원 정보가 수정되었습니다.");
        return "redirect:/member/mypage";
    }

    @PostMapping("/delete")
    public String deleteMember(@RequestParam("user_id") String user_id, HttpSession session, RedirectAttributes rttr) {
        // Service 사용
        memberService.deleteMember(user_id);
        
        // Security Context 및 세션 정리는 컨트롤러(프레젠테이션 계층)의 역할이 맞으므로 유지
        SecurityContextHolder.clearContext();
        if (session != null) {
            session.invalidate();
        }
        rttr.addFlashAttribute("msg", "정상적으로 탈퇴되었습니다.");
        return "redirect:/";
    }

    // ================= [AJAX] =================

    @PostMapping("/idCheck")
    @ResponseBody
    public String idCheck(@RequestParam("user_id") String user_id) {
        // Service 사용
        int count = memberService.checkIdDuplicate(user_id);
        return (count > 0) ? "fail" : "success";
    }
}
