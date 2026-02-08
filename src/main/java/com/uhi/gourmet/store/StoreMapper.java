/* com/uhi/gourmet/store/StoreMapper.java */
package com.uhi.gourmet.store;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StoreMapper {

    // [1] 메인 페이지용 인기 맛집 조회 (조회수 기준 상위 6개)
    List<StoreVO> selectPopularStore();

    /**
     * [수정] 맛집 리스트 조회
     * Criteria DTO를 제거하고 검색에 필요한 파라미터만 직접 받습니다.
     * 페이징 처리는 Service 계층에서 PageHelper.startPage()가 수행하므로 파라미터에서 제외됩니다.
     */
    List<StoreVO> getListStore(@Param("category") String category, 
                               @Param("region") String region, 
                               @Param("keyword") String keyword);

    // [기존 getTotalCount는 PageHelper가 자동으로 처리하므로 인터페이스에서 삭제 가능하지만, 
    //  다른 용도로 사용될 가능성을 고려해 검색 필터 기반 개수 조회 기능으로 명칭 유지 가능합니다. 
    //  여기서는 순수 PageHelper 방식을 위해 제거하거나 정제합니다.]
    int getTotalCount(@Param("category") String category, 
                      @Param("region") String region, 
                      @Param("keyword") String keyword);

    // [3] 특정 가게의 상세 정보 조회 (store_id 기준)
    StoreVO getStoreDetail(int store_id);

    // [4] 특정 가게에 등록된 메뉴 리스트 조회
    List<MenuVO> getMenuList(int store_id);

    // [5] 가게 상세 페이지 열람 시 조회수 증가
    void updateViewCount(int store_id);

    // [6] 가게 정보 등록
    void insertStore(StoreVO store);

    // [7] 점주 아이디로 해당 점주의 가게 조회
    StoreVO getStoreByUserId(String user_id);

    // [8] 가게 정보 수정
    void updateStore(StoreVO store);

    // [9] 메뉴 관리
    void insertMenu(MenuVO menu);
    void deleteMenu(int menu_id);
    void updateMenu(MenuVO menu);
    MenuVO getMenuDetail(int menu_id);
    int countMenuName(@Param("store_id") int store_id, @Param("menu_name") String menu_name);

    // [10] 특정 날짜에 점유된 예약 시간 리스트 조회
    List<String> getBookedTimes(@Param("store_id") int store_id, 
                              @Param("book_date") String book_date);
}
