/* com/uhi/gourmet/review/ReviewController.java */
package com.uhi.gourmet.review;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.github.pagehelper.PageInfo;
import com.uhi.gourmet.store.StoreService;
import com.uhi.gourmet.store.StoreVO;

@Controller
@RequestMapping("/review")
public class ReviewController {

	@Autowired
	private ReviewService review_service;

	// [추가] 리뷰 목록 헤더에 가게 정보를 표시하기 위해 주입
	@Autowired
	private StoreService store_service;

	/**
	 * [신규] 1. 특정 가게의 전체 리뷰 목록 조회 (페이징 적용) 기존 StoreController의 기능을 이관하고 PageHelper를
	 * 적용했습니다.
	 */
	@GetMapping("/list")
	public String reviewList(@RequestParam("store_id") int storeId,
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "2") int pageSize, Model model) {

		// 1. 헤더용 가게 상세 정보 조회
		StoreVO store = store_service.getStoreDetail(storeId);

		// 2. 페이징 처리된 리뷰 목록 조회 (PageInfo 반환)
		PageInfo<ReviewVO> pageMaker = review_service.getStoreReviews(storeId, pageNum, pageSize);

		model.addAttribute("store", store);
		model.addAttribute("allReviews", pageMaker.getList()); // 실제 데이터 리스트
		model.addAttribute("pageMaker", pageMaker); // 페이징 메타데이터

		return "review/review_list";
	}

	/**
	 * 2. 리뷰 작성 페이지 이동 (GET) 데이터 수집 및 자격 검증 로직을 서비스로 위임
	 */
	@GetMapping("/write")
	public String writePage(@RequestParam("store_id") int storeId,
			@RequestParam(value = "book_id", required = false) Integer bookId,
			@RequestParam(value = "wait_id", required = false) Integer waitId, Principal principal, Model model) {

		if (principal == null)
			return "redirect:/member/login";

		// 서비스에서 모든 페이지 구성 데이터를 맵으로 받아옴
		Map<String, Object> context = review_service.getReviewWriteContext(principal.getName(), storeId, bookId,
				waitId);

		if (!(boolean) context.get("isEligible")) {
			return "redirect:/wait/myStatus";
		}

		model.addAllAttributes(context);
		return "review/review_write";
	}

	/**
	 * 3. 리뷰 등록 프로세스 (POST) 서비스 내부에서 자격 재검증 및 ID 주입을 한 번에 처리
	 */
	@PostMapping("/write")
	public String writeProcess(ReviewVO vo, Principal principal, RedirectAttributes rttr) {

		if (principal == null)
			return "redirect:/member/login";

		try {
			// 비즈니스 로직(검증+저장)을 서비스 메서드 하나로 해결
			review_service.registerReview(vo, principal.getName());
			rttr.addFlashAttribute("msg", "소중한 리뷰가 등록되었습니다.");

		} catch (RuntimeException e) {
			// 서비스에서 던진 비즈니스 예외 처리
			rttr.addFlashAttribute("msg", e.getMessage());
			return "redirect:/wait/myStatus";
		}

		return "redirect:/store/detail?storeId=" + vo.getStore_id();
	}

	/**
	 * 4. 리뷰 삭제 프로세스 (권한 검증 추가)
	 */
//    @PostMapping("/delete")
//    public String deleteProcess(
//            @RequestParam("review_id") int review_id, 
//            @RequestParam("store_id") int store_id,
//            Principal principal,
//            RedirectAttributes rttr) {
//        
//        if (principal == null) {
//            rttr.addFlashAttribute("msg", "로그인이 필요합니다.");
//            return "redirect:/member/login";
//        }
//        
//        // 권한 검증: 본인이 작성한 리뷰인지 확인
//        if (!review_service.canDeleteReview(review_id, principal.getName())) {
//            rttr.addFlashAttribute("msg", "본인이 작성한 리뷰만 삭제할 수 있습니다.");
//            return "redirect:/review/list?store_id=" + store_id;
//        }
//        
//        review_service.removeReview(review_id);
//        rttr.addFlashAttribute("msg", "리뷰가 삭제되었습니다.");
//        
//        return "redirect:/review/list?store_id=" + store_id;
//    }

	@PostMapping("/delete")
	public String deleteProcess(@RequestParam("review_id") int review_id, @RequestParam("store_id") int store_id,
			@RequestParam(value = "returnUrl", required = false) String returnUrl, Principal principal,
			RedirectAttributes rttr) {

		if (principal == null) {
			rttr.addFlashAttribute("msg", "로그인이 필요합니다.");
			return "redirect:/member/login";
		}

		if (!review_service.canDeleteReview(review_id, principal.getName())) {
			rttr.addFlashAttribute("msg", "본인이 작성한 리뷰만 삭제할 수 있습니다.");
			return returnUrl != null ? "redirect:" + returnUrl : "redirect:/review/list?store_id=" + store_id;
		}

		review_service.removeReview(review_id);
		rttr.addFlashAttribute("msg", "리뷰가 삭제되었습니다.");

		// returnUrl이 있으면 해당 페이지로, 없으면 기본값
		return returnUrl != null ? "redirect:" + returnUrl : "redirect:/review/list?store_id=" + store_id;
	}
}