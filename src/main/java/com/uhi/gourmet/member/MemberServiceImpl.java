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

    @Autowired
    private BookService book_service;

    @Autowired
    private WaitService wait_service;

    /**
     * 일반 회원 가입
     * 단일 테이블 INSERT지만 트랜잭션을 통해 안전하게 커밋을 보장합니다.
     */
    @Override
    @Transactional
    public void joinMember(MemberVO member) {
        member.setUser_pw(pwEncoder.encode(member.getUser_pw()));
        member.setUser_role("ROLE_USER");
        memberMapper.join(member);
    }

    /**
     * 점주 회원 가입 (1단계 계정 + 2단계 가게 정보)
     * 두 테이블(MEMBERS, STORE)에 대한 입력이 모두 성공해야 최종 커밋됩니다.
     */
    @Override
    @Transactional
    public void joinOwner(MemberVO member, StoreVO store) {
        // 1. 회원 계정 생성
        member.setUser_pw(pwEncoder.encode(member.getUser_pw()));
        member.setUser_role("ROLE_OWNER");
        memberMapper.join(member);
        
        // 2. 가게 정보 생성 (생성된 회원 ID 연동)
        store.setUser_id(member.getUser_id());
        storeMapper.insertStore(store);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberVO getMember(String userId) {
        return memberMapper.getMemberById(userId);
    }

    /**
     * 회원 정보 수정
     * 비밀번호를 입력하지 않았을 경우(null), 기존 비밀번호를 유지하도록 처리합니다.
     */
    @Override
    @Transactional
    public void updateMember(MemberVO member) {
        if (member.getUser_pw() != null && !member.getUser_pw().trim().isEmpty()) {
            member.setUser_pw(pwEncoder.encode(member.getUser_pw()));
        } else {
            // Mapper XML에서 <if test="user_pw != null"> 처리가 필요합니다.
            member.setUser_pw(null);
        }
        memberMapper.updateMember(member);
    }

    @Override
    @Transactional
    public void deleteMember(String userId) {
        memberMapper.deleteMember(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int checkIdDuplicate(String userId) {
        return memberMapper.idCheck(userId);
    }

    /**
     * 마이페이지 통합 요약 로직
     * 조회 성능 향상을 위해 readOnly 트랜잭션을 적용합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMyStatusSummary(String userId) {
        Map<String, Object> summary = new HashMap<>();
        
        List<BookVO> my_book_list = book_service.get_my_book_list(userId);
        List<WaitVO> my_wait_list = wait_service.get_my_wait_list(userId);
        
        if (my_book_list == null) my_book_list = new ArrayList<>();
        if (my_wait_list == null) my_wait_list = new ArrayList<>();
        
        // 1. 현재 이용 중인 서비스 필터링 (WAITING, CALLED, RESERVED 등)
        summary.put("activeWait", my_wait_list.stream()
            .filter(w -> "WAITING".equals(w.getWait_status()) || "CALLED".equals(w.getWait_status()) || "ING".equals(w.getWait_status()))
            .findFirst().orElse(null));
            
        summary.put("activeBook", my_book_list.stream()
            .filter(b -> "RESERVED".equals(b.getBook_status()) || "ING".equals(b.getBook_status()))
            .findFirst().orElse(null));

        // 2. 방문 완료 히스토리 추출 (FINISH 상태)
        List<WaitVO> finishedWaits = my_wait_list.stream()
            .filter(w -> "FINISH".equals(w.getWait_status())).collect(Collectors.toList());
        
        List<BookVO> finishedBooks = my_book_list.stream()
            .filter(b -> "FINISH".equals(b.getBook_status())).collect(Collectors.toList());

        summary.put("finishedWaits", finishedWaits);
        summary.put("finishedBooks", finishedBooks);
        
        // 3. 미작성 리뷰 개수 계산 (review_id가 없는 항목 합산)
        long pendingReviewCount = finishedWaits.stream().filter(w -> w.getReview_id() == null).count()
                                + finishedBooks.stream().filter(b -> b.getReview_id() == null).count();
        
        summary.put("pendingReviewCount", pendingReviewCount);
        summary.put("my_book_list", my_book_list);
        summary.put("my_wait_list", my_wait_list);
        
        return summary;
    }
}