package com.uhi.gourmet.member;

import java.util.Date;
import lombok.Data;

@Data
public class MemberVO {
    private String user_id;      // 아이디
    private String user_pw;      // 비밀번호 (암호화되어 저장됨)
    private String user_nm;      // 이름 (기존 user_name -> user_nm)
    private String user_email;   // 이메일 (기존 user_mail -> user_email)
    private String user_tel;     // 전화번호
    
    // 선택 입력 사항
    private String user_zip;     // 우편번호
    private String user_addr1;   // 주소
    private String user_addr2;   // 상세주소
    
    // 시스템 관리 데이터 (DB Default 값 사용 예정)
    private String user_role;    // 권한 (ROLE_USER)
    private String enabled;      // 계정사용여부 (Y)
    private Date user_regdate;   // 가입일
    
    // 추가된 위도/경도
    private Double user_lat;
    private Double user_lon;
}