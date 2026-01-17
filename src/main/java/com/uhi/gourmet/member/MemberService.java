/* com/uhi/gourmet/member/MemberService.java */
package com.uhi.gourmet.member;

import java.util.Map;
import com.uhi.gourmet.member.MemberVO;
import com.uhi.gourmet.store.StoreVO;

public interface MemberService {

    void joinMember(MemberVO member);

    void joinOwner(MemberVO member, StoreVO store);

    MemberVO getMember(String userId);

    void updateMember(MemberVO member);

    void deleteMember(String userId);

    int checkIdDuplicate(String userId);
    
    Map<String, Object> getMyStatusSummary(String userId);
}