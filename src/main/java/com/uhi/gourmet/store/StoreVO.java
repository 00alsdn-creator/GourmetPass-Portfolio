package com.uhi.gourmet.store;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoreVO {
    private int store_id;
    private String user_id;
    private String store_name;
    private String store_category;
    private String store_tel;
    private String store_zip;
    private String store_addr1;
    private String store_addr2;
    private Double store_lat;
    private Double store_lon;
    private int store_cnt;          // 조회수 (DB 컬럼명 일치)
    private String store_desc;
    private String store_img;
    private String open_time;
    private String close_time;
    private int res_unit;
    private int max_capacity; // 매장 최대 수용인원

    // [통계용 추가 필드] 명칭 통일 규칙 적용
    private double avg_rating;      // 평균 별점
    private int review_cnt;         // _cnt로 변경 (기존 review_count 제거)
}