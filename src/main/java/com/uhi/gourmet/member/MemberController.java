package com.uhi.gourmet.member;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // 암호화 도구
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody; // AJAX 통신용'

import com.uhi.gourmet.store.StoreMapper;
import com.uhi.gourmet.store.StoreVO; // 다른 도메인은 import 필요

@Controller
public class MemberController {

	@Autowired
	private MemberMapper memberMapper;

	@Autowired
	private StoreMapper storeMapper;

	@Autowired // security-context.xml에서 설정한 암호화 객체 주입
	private BCryptPasswordEncoder pwEncoder;

	// api.properties의 키값을 가져옴 (없으면 빈값)
	@Value("${kakao.js.key}")
	private String kakaoKey;

	@GetMapping("/join.do")
	public String joinPage(Model model) {
		// 디버깅용: 콘솔에 값이 찍히는지 확인 (서버 실행 시 Console 탭 확인)
		System.out.println(">>> 로드된 카카오 키: " + kakaoKey);

		model.addAttribute("kakaoKey", kakaoKey);
		return "member/join";
	}

	// [1] 메인/로그인 화면
	@RequestMapping(value = { "/", "/login.do" }, method = RequestMethod.GET)
	public String index() {
		return "modeltwo/login";
	}

	// [2] 로그인 처리 (암호화된 비번 비교)
	@RequestMapping(value = "/login.do", method = RequestMethod.POST)
	public String login(MemberVO vo, HttpSession session) {
		String dbPw = memberMapper.getPassword(vo.getUser_id());

		if (dbPw != null && pwEncoder.matches(vo.getUser_pw(), dbPw)) {
			session.setAttribute("loginID", vo.getUser_id());
			return "modeltwo/main";
		} else {
			return "redirect:/?error=true";
		}
	}

	// [3] 로그아웃
	@RequestMapping(value = "/logout.do", method = RequestMethod.GET)
	public String logout(HttpSession session) {
		session.invalidate();
		return "modeltwo/logoutMsg";
	}

	// ================= [회원가입 공통/일반] =================

	// [4-1] 회원가입 유형 선택 페이지
	@RequestMapping(value = "/join/select.do", method = RequestMethod.GET)
	public String joinSelect() {
		return "modeltwo/signup_select";
	}

	// [4-2] 일반 회원가입 페이지 이동
	@RequestMapping(value = "/join/general.do", method = RequestMethod.GET)
	public String joinGeneralPage() {
		return "modeltwo/join";
	}

	// [4-3] 일반 회원가입 처리 (암호화 + 좌표 저장)
	@RequestMapping(value = "/joinProcess.do", method = RequestMethod.POST)
	public String joinProcess(MemberVO vo) {
		String encodePw = pwEncoder.encode(vo.getUser_pw());
		vo.setUser_pw(encodePw);
		memberMapper.join(vo);
		return "redirect:/";
	}

	// ================= [점주 회원가입: 멀티 페이지 방식] =================

	// [5-1] 점주 가입 1단계: 사장님 개인 정보 입력 페이지
	@RequestMapping(value = "/join/owner1.do", method = RequestMethod.GET)
	public String ownerStep1() {
		// 기존 signup_owner.jsp 대신 signup_owner1.jsp를 호출하여 404 방지
		return "modeltwo/signup_owner1";
	}

	// [5-2] 1단계 데이터 처리: 세션 저장 및 2단계 이동
	@RequestMapping(value = "/join/ownerStep1.do", method = RequestMethod.POST)
	public String ownerStep1Process(MemberVO memberVo, HttpSession session) {
		// 1단계 입력값(개인좌표 포함)을 세션에 임시 저장
		session.setAttribute("tempMember", memberVo);
		return "redirect:/join/owner2.do";
	}

	// [5-3] 점주 가입 2단계: 가게 정보 입력 페이지
	@RequestMapping(value = "/join/owner2.do", method = RequestMethod.GET)
	public String ownerStep2(HttpSession session) {
		// 1단계 데이터가 없는데 2단계로 바로 접속하려 할 경우 차단
		if (session.getAttribute("tempMember") == null) {
			return "redirect:/join/owner1.do";
		}
		return "modeltwo/signup_owner2";
	}

	// [5-4] 최종 가입 처리: 세션 데이터(회원) + 폼 데이터(가게) 합쳐서 DB 저장
	@RequestMapping(value = "/join/ownerFinal.do", method = RequestMethod.POST)
	public String ownerFinalProcess(StoreVO storeVo, HttpSession session) {
		// 1. 세션에서 1단계 회원 데이터 꺼내기
		MemberVO memberVo = (MemberVO) session.getAttribute("tempMember");
		if (memberVo == null)
			return "redirect:/join/owner1.do";

		// 2. 회원 정보 암호화 및 권한(OWNER) 설정
		String encodePw = pwEncoder.encode(memberVo.getUser_pw());
		memberVo.setUser_pw(encodePw);
		memberVo.setUser_role("ROLE_OWNER");

		// 3. DB 저장: MEMBERS 테이블
		memberMapper.join(memberVo);

		// 4. DB 저장: STORE 테이블 (방금 가입한 아이디 FK 연결)
		storeVo.setUser_id(memberVo.getUser_id());
		storeMapper.insertStore(storeVo);

		// 5. 완료 후 세션 비우기
		session.removeAttribute("tempMember");

		return "redirect:/login.do";
	}

	// ================= [공통 기능] =================

	// [6] 아이디 중복 확인 (AJAX)
	@RequestMapping(value = "/idCheck.do", method = RequestMethod.POST)
	@ResponseBody
	public String idCheck(@RequestParam("user_id") String user_id) {
		int count = memberMapper.idCheck(user_id);
		return (count > 0) ? "fail" : "success";
	}
}