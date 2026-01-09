package com.uhi.gourmet.member;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface MemberMapper {

    // 1. 비밀번호 조회
    String getPassword(@Param("user_id") String user_id);

    // 2. 회원 가입
    void join(MemberVO vo);

    // 3. 아이디 중복 체크
    int idCheck(String user_id);

    // 4. 회원 상세 정보 조회
    MemberVO getMemberById(String user_id);

    // 5. 회원 정보 수정(VO의 user_pw가 null이면 비밀번호는 수정하지 않음)
    void updateMember(MemberVO vo);

    // 6. 회원 탈퇴 (추가!)
    void deleteMember(String user_id);
}