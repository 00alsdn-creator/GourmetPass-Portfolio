/* com/uhi/gourmet/store/StoreService.java */
package com.uhi.gourmet.store;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.github.pagehelper.PageInfo;

/**
 * 맛집 관련 비즈니스 로직 인터페이스
 */
public interface StoreService {
    
    // 1. 맛집 목록 및 상세 조회
    /**
     * [수정] 맛집 목록 조회 (PageHelper 적용)
     * @param pageNum  현재 페이지 번호
     * @param pageSize 페이지당 출력할 데이터 개수
     * @param category 카테고리 필터
     * @param region   지역 필터
     * @param keyword  검색어 필터
     * @return 페이징 정보(PageInfo)가 포함된 맛집 결과
     */
    PageInfo<StoreVO> getStoreList(int pageNum, int pageSize, String category, String region, String keyword);

    /**
     * [수정] 전체 데이터 개수 조회
     * @param category 카테고리 필터
     * @param region   지역 필터
     * @param keyword  검색어 필터
     * @return 검색 조건에 해당하는 총 행 수
     */
    int getTotal(String category, String region, String keyword);

    /**
     * [신규] 메인 페이지용 인기 맛집 조회
     * 조회수(store_cnt) 기준 상위 6개의 매장을 반환합니다.
     */
    List<StoreVO> getPopularStores();

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

    // [추가 및 수정 로직]
    List<String> getAvailableTimeSlots(StoreVO store, String bookDate);
    List<String> generateTimeSlots(StoreVO store);
    String uploadFile(MultipartFile file, String realPath);
}