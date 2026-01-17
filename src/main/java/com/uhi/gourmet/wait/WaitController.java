/* com/uhi/gourmet/wait/WaitController.java */
package com.uhi.gourmet.wait;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/wait")
public class WaitController {

    @Autowired
    private WaitService wait_service;

    // [리팩토링] 로직이 서비스로 이전됨에 따라 타 도메인 서비스(BookService) 직접 주입 제거
    
    @Autowired
    private SimpMessagingTemplate messaging_template;

    /**
     * 일반 회원: 내 이용현황 페이지 조회
     * [수정] 복잡한 필터링 및 Stream 로직을 WaitService.getMyStatusSummary로 위임
     */
    @GetMapping("/myStatus")
    public String my_status(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/member/login";
        }
        
        String user_id = principal.getName();
        
        // [수정 포인트] 서비스 계층에서 가공된 모든 상태 데이터를 한 번에 모델에 추가
        model.addAllAttributes(wait_service.getMyStatusSummary(user_id));
        
        return "wait/myStatus";
    }

    @PostMapping("/register")
    public String register_wait(WaitVO vo, Principal principal) {
        if (principal == null) {
            return "redirect:/member/login";
        }
        vo.setUser_id(principal.getName());
        wait_service.register_wait(vo);
        
        // 실시간 알림 전송
        messaging_template.convertAndSend("/topic/store/" + vo.getStore_id(), 
            "새로운 웨이팅이 접수되었습니다! 번호: " + vo.getWait_num());
        
        return "redirect:/wait/myStatus";
    }

    @PostMapping("/updateStatus")
    public String update_status(@RequestParam("wait_id") int wait_id, 
                                @RequestParam(value="user_id", required=false) String user_id,
                                @RequestParam("status") String status) {
        
        wait_service.update_wait_status(wait_id, status);
        
        // 상태가 'CALLED'일 경우 해당 유저에게 실시간 알림 발송
        if ("CALLED".equals(status) && user_id != null) {
            messaging_template.convertAndSend("/topic/wait/" + user_id, 
                "입장하실 순서입니다! 매장으로 방문해주세요.");
        }
        
        return "redirect:/book/manage";
    }

    @PostMapping("/cancel")
    public String cancel_wait(@RequestParam("wait_id") int wait_id) {
        wait_service.update_wait_status(wait_id, "CANCELLED");
        return "redirect:/wait/myStatus";
    }
}