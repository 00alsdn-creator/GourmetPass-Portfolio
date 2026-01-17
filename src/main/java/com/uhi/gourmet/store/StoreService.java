/* com/uhi/gourmet/store/StoreService.java */
package com.uhi.gourmet.store;

import java.util.List;
import org.springframework.web.multipart.MultipartFile; // 추가

public interface StoreService {
    // 1. 맛집 목록 및 상세 조회 (Public)
    List<StoreVO> getStoreList(String category, String region, String keyword);
    StoreVO getStoreDetail(int storeId);
    List<MenuVO> getMenuList(int storeId);
    void plusViewCount(int storeId);
    
    // 2. 가게 관리 (Owner Only)
    void registerStore(StoreVO vo, String userId);
    void modifyStore(StoreVO vo, String userId);
    StoreVO getMyStore(int storeId, String userId); 
    StoreVO get_store_by_user_id(String userId);    
    
    // 3. 메뉴 관리 (Owner Only)
    void addMenu(MenuVO vo, String userId);
    void removeMenu(int menuId, String userId);
    MenuVO getMenuDetail(int menuId, String userId); 
    void modifyMenu(MenuVO vo, String userId);

    // [수정 포인트: 비즈니스 로직 명세 추가]
    List<String> generateTimeSlots(StoreVO store);
    String uploadFile(MultipartFile file, String realPath);
}