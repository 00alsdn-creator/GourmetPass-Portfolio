package com.uhi.gourmet.store;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // @Value 사용을 위해 추가
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private StoreMapper storeMapper;

    // [중요] api.properties에 설정된 카카오 JS 키를 읽어옵니다.
    // 설정 파일에 kakao.js.key=값 형식으로 저장되어 있어야 합니다.
    @Value("${kakao.js.key}")
    private String kakaoJsKey;

    // 1. 맛집 목록 조회
    @GetMapping("/list")
    public String storeList(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "region", required = false) String region,
            Model model) {
        
        List<StoreVO> storeList = storeMapper.getListStore(category, region);
        
        model.addAttribute("storeList", storeList); 
        model.addAttribute("category", category);
        model.addAttribute("region", region);
        
        return "store/store_list";
    }

    // 2. 맛집 상세 정보 조회 (카카오 지도 키 전달 추가)
    @GetMapping("/detail")
    public String storeDetail(@RequestParam("storeId") int storeId, Model model) {
        
        // 1. 조회수 상승
        storeMapper.updateViewCount(storeId);
        
        // 2. 가게 상세 정보 조회
        StoreVO store = storeMapper.getStoreDetail(storeId);
        
        // 3. 해당 가게의 메뉴 목록 조회
        List<MenuVO> menuList = storeMapper.getMenuList(storeId);
        
        // 4. 모델에 담아서 JSP로 전달
        model.addAttribute("store", store);
        model.addAttribute("menuList", menuList);
        
        // [핵심] JSP에서 카카오 지도를 띄울 수 있도록 키값을 전달합니다.
        // JSP에서는 ${kakaoJsKey}로 꺼내 쓰게 됩니다.
        model.addAttribute("kakaoJsKey", kakaoJsKey);
        
        return "store/store_detail";
    }
    
    /*
     * [AJAX 응답 메서드] 
     * 타임테이블을 서버에서 직접 계산하여 보낼 때 사용합니다.
     */
    @GetMapping(value = "/api/timeSlots", produces = "application/json; charset=UTF-8")
    @ResponseBody 
    public List<String> getTimeSlots(@RequestParam("store_id") int storeId) {
        List<String> slots = new ArrayList<>();
        try {
            StoreVO store = storeMapper.getStoreDetail(storeId);
            
            if (store == null || store.getOpen_time() == null || store.getClose_time() == null) {
                return slots;
            }

            // DB 형식이 "09:00" 인지 확인 필요 (LocalTime.parse는 HH:mm 형식을 인식함)
            LocalTime open = LocalTime.parse(store.getOpen_time());
            LocalTime close = LocalTime.parse(store.getClose_time());
            int unit = store.getRes_unit() == 0 ? 30 : store.getRes_unit(); // 0분 방지

            LocalTime current = open;
            while (current.isBefore(close)) {
                slots.add(current.toString());
                current = current.plusMinutes(unit);
            }
        } catch (Exception e) {
            System.err.println("타임슬롯 생성 중 에러: " + e.getMessage());
        }
        return slots; 
    }
}