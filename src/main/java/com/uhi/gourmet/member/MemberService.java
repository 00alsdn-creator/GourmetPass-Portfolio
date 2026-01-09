package com.uhi.gourmet.member;

import com.uhi.gourmet.member.MemberVO;
import com.uhi.gourmet.store.StoreVO;

public interface MemberService {

    /**
     * 일반 회원 가입 처리
     * 一般会員登録処理
     */
    void joinMember(MemberVO member);

    /**
     * 오너 회원 가입 처리 (회원 정보 + 가게 정보 트랜잭션)
     * オーナー会員登録処理（会員情報＋店舗情報のトランザクション）
     */
    void joinOwner(MemberVO member, StoreVO store);

    /**
     * 회원 정보 조회
     * 会員情報の照会
     */
    MemberVO getMember(String userId);

    /**
     * 회원 정보 수정
     * 会員情報の修正
     */
    void updateMember(MemberVO member);

    /**
     * 회원 탈퇴
     * 会員退会
     */
    void deleteMember(String userId);

    /**
     * 아이디 중복 체크
     * ID重複チェック
     */
    int checkIdDuplicate(String userId);
}
