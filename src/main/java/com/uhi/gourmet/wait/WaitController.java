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

    @Autowired
    private SimpMessagingTemplate messaging_template;

    /**
     * [1] 나의 실시간 이용 현황 페이지 조회
     */
    @GetMapping("/status")
    public String my_status(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/member/login";
        }
        
        String user_id = principal.getName();
        
        // 서비스 계층에서 가공된 모든 상태 데이터를 한 번에 모델에 추가
        model.addAllAttributes(wait_service.getMyStatusSummary(user_id));
        
        return "wait/wait_status";
    }

    /**
     * [2] 웨이팅 등록
     */
    @PostMapping("/register")
    public String register_wait(WaitVO vo, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/member/login";
        }
        
        String user_id = principal.getName();
        vo.setUser_id(user_id);
        
        try {
            // 중복 체크 + 등록은 전부 서비스에서
            wait_service.register_wait(vo);

        } catch (IllegalStateException e) {
            model.addAttribute("msg", e.getMessage());
            model.addAttribute("url", "/wait/status");
            return "common/alert";
        }
    
        // 점주에게 실시간 알림 전송 (새로운 웨이팅 발생)
        messaging_template.convertAndSend("/topic/store/" + vo.getStore_id(), 
            "새로운 웨이팅이 접수되었습니다! 번호: " + vo.getWait_num());
            
        // [추가] 대기열에 변화가 생겼으므로 해당 가게의 모든 대기자 화면 갱신 신호 발송
        messaging_template.convertAndSend("/topic/store/" + vo.getStore_id() + "/waitUpdate", "REFRESH");
        
        return "redirect:/wait/status";
    }

    /**
     * [핵심 리팩토링] [3] 상태 업데이트 (점주 호출/입장/취소)
     * 해결: 누군가 입장하면 대기 중인 '모든' 사용자의 대기 팀 수가 즉시 줄어들도록 전체 신호를 보냅니다.
     */
    @PostMapping("/updateStatus")
    public String update_status(@RequestParam("wait_id") int wait_id, 
                                @RequestParam(value="user_id", required=false) String user_id,
                                @RequestParam("status") String status) {
        
        // 1. 상태 업데이트 전, 해당 웨이팅 정보를 조회하여 어느 가게(store_id)인지 파악합니다.
        WaitVO wait = wait_service.get_wait_detail(wait_id); 
        
        // 2. 실제 DB 상태 업데이트 수행
        wait_service.update_wait_status(wait_id, status);
        
        if (wait != null) {
            int store_id = wait.getStore_id();

            // 3. [개별 알림] 호출(CALLED) 시 해당 유저 한 명에게만 "입장하세요" 신호 (기존)
            if ("CALLED".equals(status) && user_id != null) {
                messaging_template.convertAndSend("/topic/wait/" + user_id, status);
            }
            
            // 4. [전체 방송] 상태가 'ING'(식사중)이나 'CANCELLED'로 변했다면 대기 팀 수가 변한 것이므로
            // 해당 가게 채널(/topic/store/{id}/waitUpdate)을 구독 중인 모든 유저에게 새로고침 신호 전송
            messaging_template.convertAndSend("/topic/store/" + store_id + "/waitUpdate", "REFRESH");
        }
        
        return "redirect:/book/manage";
    }

    /**
     * [4] 웨이팅 취소
     */
    @PostMapping("/cancel")
    public String cancel_wait(@RequestParam("wait_id") int wait_id) {
        WaitVO wait = wait_service.get_wait_detail(wait_id);
        wait_service.update_wait_status(wait_id, "CANCELLED");
        
        if (wait != null) {
            // 내가 취소해도 다른 사람들의 대기 팀 수가 줄어들어야 하므로 전체 신호 발송
            messaging_template.convertAndSend("/topic/store/" + wait.getStore_id() + "/waitUpdate", "REFRESH");
        }
        
        return "redirect:/wait/status";
    }
}