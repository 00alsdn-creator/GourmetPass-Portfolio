/* com/uhi/gourmet/store/StoreServiceImpl.java */
package com.uhi.gourmet.store;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreMapper storeMapper;

    // 1. 맛집 목록 조회 (PageHelper 반영)
    @Override
    public PageInfo<StoreVO> getStoreList(int pageNum, int pageSize, String category, String region, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<StoreVO> list = storeMapper.getListStore(category, region, keyword);
        return new PageInfo<>(list);
    }

    @Override
    public int getTotal(String category, String region, String keyword) {
        return storeMapper.getTotalCount(category, region, keyword);
    }

    @Override
    public List<StoreVO> getPopularStores() {
        return storeMapper.selectPopularStore();
    }

    @Override
    @Transactional
    public StoreVO getStoreDetail(int storeId) {
        return storeMapper.getStoreDetail(storeId);
    }

    @Override
    public List<MenuVO> getMenuList(int storeId) {
        return storeMapper.getMenuList(storeId);
    }

    @Override
    public void plusViewCount(int storeId) {
        storeMapper.updateViewCount(storeId);
    }

    // 5. 가게 등록
    @Override
    @Transactional
    public void registerStore(StoreVO vo, String userId) {
        vo.setUser_id(userId);
        storeMapper.insertStore(vo);
    }

    // 6. 가게 정보 수정
    @Override
    @Transactional
    public void modifyStore(StoreVO vo, String userId) {
        // [수정 포인트] ID가 0이면 수정 대상이 아니므로 예외를 던져 롤백하거나 중단합니다.
        if (vo.getStore_id() <= 0) {
            throw new RuntimeException("유효하지 않은 가게 ID입니다. (ID: 0)");
        }

        StoreVO check = storeMapper.getStoreDetail(vo.getStore_id());
        if (check != null && check.getUser_id().equals(userId)) {
            vo.setUser_id(userId);
            storeMapper.updateStore(vo);
        } else {
            throw new RuntimeException("가게 수정 권한이 없거나 해당 가게가 존재하지 않습니다.");
        }
    }

    @Override
    public StoreVO getMyStore(int storeId, String userId) {
        StoreVO store = storeMapper.getStoreDetail(storeId);
        if (store != null && store.getUser_id().equals(userId)) {
            return store;
        }
        return null;
    }

    @Override
    public StoreVO get_store_by_user_id(String userId) {
        return storeMapper.getStoreByUserId(userId);
    }

    // 9. 메뉴 추가
    @Override
    @Transactional
    public void addMenu(MenuVO vo, String userId) {
        // [수정 포인트] store_id가 0으로 넘어올 경우 FK 제약조건 위반을 방지합니다.
        if (vo.getStore_id() <= 0) {
            throw new RuntimeException("가게 정보(ID)가 누락되었습니다.");
        }

        StoreVO store = storeMapper.getStoreDetail(vo.getStore_id());
        if (store != null && store.getUser_id().equals(userId)) {
            // [추가] 빈 문자열("")이 들어올 경우 DB 제약조건 위반 방지를 위해 'N'으로 세팅
            if (vo.getMenu_sign() == null || vo.getMenu_sign().trim().isEmpty()) {
                vo.setMenu_sign("N");
            }
            storeMapper.insertMenu(vo);
        } else {
            throw new RuntimeException("메뉴 등록 권한이 없습니다.");
        }
    }

    // 10. 메뉴 삭제
    @Override
    @Transactional
    public void removeMenu(int menuId, String userId) {
        MenuVO menu = storeMapper.getMenuDetail(menuId);
        if (menu != null) {
            StoreVO store = storeMapper.getStoreDetail(menu.getStore_id());
            if (store != null && store.getUser_id().equals(userId)) {
                storeMapper.deleteMenu(menuId);
            }
        }
    }

    @Override
    public MenuVO getMenuDetail(int menuId, String userId) {
        MenuVO menu = storeMapper.getMenuDetail(menuId);
        if (menu != null) {
            StoreVO store = storeMapper.getStoreDetail(menu.getStore_id());
            if (store != null && store.getUser_id().equals(userId)) {
                return menu;
            }
        }
        return null;
    }

    // 12. 메뉴 수정
    @Override
    @Transactional
    public void modifyMenu(MenuVO vo, String userId) {
        // [수정 포인트] 0번 메뉴 수정 방지
        if (vo.getMenu_id() <= 0) {
            throw new RuntimeException("유효하지 않은 메뉴 ID입니다.");
        }

        MenuVO menu = storeMapper.getMenuDetail(vo.getMenu_id());
        if (menu != null) {
            StoreVO store = storeMapper.getStoreDetail(menu.getStore_id());
            if (store != null && store.getUser_id().equals(userId)) {
                if (vo.getMenu_sign() == null || vo.getMenu_sign().trim().isEmpty()) {
                    vo.setMenu_sign("N");
                }
                storeMapper.updateMenu(vo);
            }
        }
    }

    // 13. 예약 가능 시간 슬롯 동적 생성
    @Override
    public List<String> getAvailableTimeSlots(StoreVO store, String bookDate) {
        List<String> allSlots = generateTimeSlots(store);
        List<String> bookedTimes = storeMapper.getBookedTimes(store.getStore_id(), bookDate);
        if (bookedTimes != null && !bookedTimes.isEmpty()) {
            allSlots.removeAll(bookedTimes);
        }
        return allSlots;
    }

    // 14. 기초 시간 슬롯 생성 로직
    @Override
    public List<String> generateTimeSlots(StoreVO store) {
        List<String> slots = new ArrayList<>();
        if (store == null || store.getOpen_time() == null || store.getClose_time() == null) {
            return slots;
        }

        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("HH:mm[:ss]");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalTime open = LocalTime.parse(store.getOpen_time(), inputFormatter);
            LocalTime close = LocalTime.parse(store.getClose_time(), inputFormatter);
            int unit = (store.getRes_unit() <= 0) ? 30 : store.getRes_unit();

            LocalTime current = open;
            while (current.isBefore(close)) {
                slots.add(current.format(outputFormatter));
                current = current.plusMinutes(unit);
            }
        } catch (Exception e) {
            System.err.println("TimeSlot Generation Error: " + e.getMessage());
        }
        return slots;
    }

    // 15. 파일 업로드 처리
    @Override
    public String uploadFile(MultipartFile file, String realPath) {
        File dir = new File(realPath);
        if (!dir.exists()) dir.mkdirs();

        String originalName = file.getOriginalFilename();
        String savedName = System.currentTimeMillis() + "_" + originalName;

        try {
            file.transferTo(new File(realPath, savedName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return savedName;
    }
}