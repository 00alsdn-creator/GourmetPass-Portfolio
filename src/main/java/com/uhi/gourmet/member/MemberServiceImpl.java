package com.uhi.gourmet.member;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uhi.gourmet.member.MemberMapper;
import com.uhi.gourmet.member.MemberVO;
import com.uhi.gourmet.member.MemberService;
import com.uhi.gourmet.store.StoreMapper;
import com.uhi.gourmet.store.StoreVO;

@Service
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private StoreMapper storeMapper; // 오너 가입 시 가게 등록을 위해 필요

    @Autowired
    private BCryptPasswordEncoder pwEncoder;

    @Override
    public void joinMember(MemberVO member) {
        // 비밀번호 암호화 및 권한 설정
        member.setUser_pw(pwEncoder.encode(member.getUser_pw()));
        member.setUser_role("ROLE_USER");
        memberMapper.join(member);
    }

    @Override
    @Transactional // 회원정보 저장과 가게정보 저장은 하나의 트랜잭션으로 묶여야 함
    public void joinOwner(MemberVO member, StoreVO store) {
        // 1. 회원 정보 저장 (암호화 및 권한 설정)
        member.setUser_pw(pwEncoder.encode(member.getUser_pw()));
        member.setUser_role("ROLE_OWNER");
        memberMapper.join(member);

        // 2. 가게 정보 저장 (회원 ID FK 연결)
        store.setUser_id(member.getUser_id());
        storeMapper.insertStore(store);
    }

    @Override
    public MemberVO getMember(String userId) {
        return memberMapper.getMemberById(userId);
    }

    @Override
    public void updateMember(MemberVO member) {
        // 비밀번호 변경 로직: 입력값이 있을 때만 암호화
        if (member.getUser_pw() != null && !member.getUser_pw().isEmpty()) {
            String encodedPw = pwEncoder.encode(member.getUser_pw());
            member.setUser_pw(encodedPw);
        } else {
            // 입력값이 없으면 null 처리하여 Mapper XML의 <if> 태그에서 무시되도록 함
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
}
