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

@Controller
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService review_service;

    /**
     * 1. 리뷰 작성 페이지 이동 (GET)
     * [리팩토링] 데이터 수집 및 자격 검증 로직을 서비스로 위임
     */
    @GetMapping("/write")
    public String writePage(@RequestParam("store_id") int storeId,
                            @RequestParam(value="book_id", required=false) Integer bookId,
                            @RequestParam(value="wait_id", required=false) Integer waitId,
                            Principal principal, Model model) {
        
        if (principal == null) return "redirect:/member/login";

        // [수정 포인트] 서비스에서 모든 페이지 구성 데이터를 맵으로 받아옴
        Map<String, Object> context = review_service.getReviewWriteContext(principal.getName(), storeId, bookId, waitId);
        
        if (!(boolean) context.get("isEligible")) {
            return "redirect:/wait/myStatus";
        }

        model.addAllAttributes(context);
        return "review/review_write";
    }

    /**
     * 2. 리뷰 등록 프로세스 (POST)
     * [리팩토링] 서비스 내부에서 자격 재검증 및 ID 주입을 한 번에 처리
     */
    @PostMapping("/write")
    public String writeProcess(ReviewVO vo, Principal principal, RedirectAttributes rttr) {
        
        if (principal == null) return "redirect:/member/login";
        
        try {
            // [수정 포인트] 비즈니스 로직(검증+저장)을 서비스 메서드 하나로 해결
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
     * 3. 리뷰 삭제 프로세스
     */
    @PostMapping("/delete")
    public String deleteProcess(@RequestParam("review_id") int review_id, 
                                @RequestParam("store_id") int store_id,
                                RedirectAttributes rttr) {
        
        review_service.removeReview(review_id);
        rttr.addFlashAttribute("msg", "리뷰가 삭제되었습니다.");
        
        return "redirect:/store/detail?storeId=" + store_id;
    }
}