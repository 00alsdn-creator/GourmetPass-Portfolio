/* com/uhi/gourmet/member/MemberServiceImpl.java */
package com.uhi.gourmet.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uhi.gourmet.store.StoreMapper;
import com.uhi.gourmet.store.StoreVO;
import com.uhi.gourmet.book.BookService;
import com.uhi.gourmet.book.BookVO;
import com.uhi.gourmet.wait.WaitService;
import com.uhi.gourmet.wait.WaitVO;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private StoreMapper storeMapper; 

    @Autowired
    private BCryptPasswordEncoder pwEncoder;

    // [수정 포인트: 타 도메인 서비스 주입]
    @Autowired
    private BookService book_service;

    @Autowired
    private WaitService wait_service;

    @Override
    public void joinMember(MemberVO member) {
        member.setUser_pw(pwEncoder.encode(member.getUser_pw()));
        member.setUser_role("ROLE_USER");
        memberMapper.join(member);
    }

    @Override
    @Transactional
    public void joinOwner(MemberVO member, StoreVO store) {
        member.setUser_pw(pwEncoder.encode(member.getUser_pw()));
        member.setUser_role("ROLE_OWNER");
        memberMapper.join(member);
        store.setUser_id(member.getUser_id());
        storeMapper.insertStore(store);
    }

    @Override
    public MemberVO getMember(String userId) {
        return memberMapper.getMemberById(userId);
    }

    @Override
    public void updateMember(MemberVO member) {
        if (member.getUser_pw() != null && !member.getUser_pw().isEmpty()) {
            member.setUser_pw(pwEncoder.encode(member.getUser_pw()));
        } else {
            member.setUser_pw(null);
        }
        memberMapper.updateMember(member);
    }

    @Override
    public void deleteMember(String userId) {
        memberMapper.deleteMember(userId);
    }

    @Override
    public int checkIdDuplicate(String userId) {
        return memberMapper.idCheck(userId);
    }

    // [수정 포인트: 마이페이지 통합 요약 로직 구현]
    @Override
    public Map<String, Object> getMyStatusSummary(String userId) {
        Map<String, Object> summary = new HashMap<>();
        
        List<BookVO> my_book_list = book_service.get_my_book_list(userId);
        List<WaitVO> my_wait_list = wait_service.get_my_wait_list(userId);
        
        if (my_book_list == null) my_book_list = new ArrayList<>();
        if (my_wait_list == null) my_wait_list = new ArrayList<>();
        
        // 1. 현재 이용 중인 서비스 필터링
        summary.put("activeWait", my_wait_list.stream()
            .filter(w -> "WAITING".equals(w.getWait_status()) || "CALLED".equals(w.getWait_status()) || "ING".equals(w.getWait_status()))
            .findFirst().orElse(null));
            
        summary.put("activeBook", my_book_list.stream()
            .filter(b -> "RESERVED".equals(b.getBook_status()) || "ING".equals(b.getBook_status()))
            .findFirst().orElse(null));

        // 2. 방문 완료 히스토리 추출
        List<WaitVO> finishedWaits = my_wait_list.stream()
            .filter(w -> "FINISH".equals(w.getWait_status())).collect(Collectors.toList());
        
        List<BookVO> finishedBooks = my_book_list.stream()
            .filter(b -> "FINISH".equals(b.getBook_status())).collect(Collectors.toList());

        summary.put("finishedWaits", finishedWaits);
        summary.put("finishedBooks", finishedBooks);
        
        // 3. 미작성 리뷰 개수 계산
        long pendingReviewCount = finishedWaits.stream().filter(w -> w.getReview_id() == null).count()
                                + finishedBooks.stream().filter(b -> b.getReview_id() == null).count();
        
        summary.put("pendingReviewCount", pendingReviewCount);
        summary.put("my_book_list", my_book_list);
        summary.put("my_wait_list", my_wait_list);
        
        return summary;
    }
}