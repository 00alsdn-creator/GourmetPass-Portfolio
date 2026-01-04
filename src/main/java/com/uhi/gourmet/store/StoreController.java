package com.uhi.gourmet.store;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private StoreMapper storeMapper;

    // 1. 맛집 목록 조회
    @GetMapping("/list")
    public String storeList(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "region", required = false) String region,
            Model model) {
        
        // 검색 조건(카테고리, 지역)을 담아 리스트 조회
        // (참고: Mapper에서 검색 필터 처리가 되어 있어야 합니다)
        List<StoreVO> storeList = storeMapper.getListStore(category, region);
        
        model.addAttribute("storeList", storeList); 
        model.addAttribute("category", category);
        model.addAttribute("region", region);
        
        return "store/store_list"; // 파일명 store_list.jsp와 매칭 [cite: 1]
    }

    // 2. 맛집 상세 정보 조회
    @GetMapping("/detail")
    public String storeDetail(@RequestParam("storeId") int storeId, Model model) {
        
        // 1. 조회수 상승
        storeMapper.updateViewCount(storeId);
        
        // 2. 가게 상세 정보 조회
        StoreVO store = storeMapper.getStoreDetail(storeId);
        
        // 3. 해당 가게의 메뉴 목록 조회 (추가된 부분)
        List<MenuVO> menuList = storeMapper.getMenuList(storeId);
        
        // 4. 모델에 담아서 JSP로 전달
        model.addAttribute("store", store);
        model.addAttribute("menuList", menuList); // 화면에서 ${menuList}로 사용
        
        return "store/store_detail";
    }
    
}