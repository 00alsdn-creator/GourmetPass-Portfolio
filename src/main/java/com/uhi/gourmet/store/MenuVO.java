package com.uhi.gourmet.store;

import lombok.Data;

@Data
public class MenuVO {
    private int menu_id;
    private int store_id;
    private String menu_name;
    private int menu_price;
    private String menu_img;
    private String menu_sign; // 대표 메뉴 여부 (Y/N)
}