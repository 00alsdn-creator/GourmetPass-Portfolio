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

@Service
public class StoreServiceImpl implements StoreService {

    @Autowired
    private StoreMapper storeMapper;

    @Override
    public List<StoreVO> getStoreList(String category, String region, String keyword) {
        // 3개의 인자(category, region, keyword)를 모두 mapper에 전달
        return storeMapper.getListStore(category, region, keyword);
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

    @Override
    @Transactional
    public void registerStore(StoreVO vo, String userId) {
        vo.setUser_id(userId); // 점주 ID 세팅
        storeMapper.insertStore(vo);
    }

    @Override
    @Transactional
    public void modifyStore(StoreVO vo, String userId) {
        // 소유권 확인
        StoreVO check = storeMapper.getStoreDetail(vo.getStore_id());
        if (check != null && check.getUser_id().equals(userId)) {
            vo.setUser_id(userId);
            storeMapper.updateStore(vo);
        }
    }

    @Override
    public StoreVO getMyStore(int storeId, String userId) {
        StoreVO store = storeMapper.getStoreDetail(storeId);
        // 내 가게가 아니면 null 반환 (컨트롤러에서 예외 처리)
        if (store != null && store.getUser_id().equals(userId)) {
            return store;
        }
        return null;
    }

    // 점주 ID로 가게 정보 조회 (MemberController에서 사용)
    @Override
    public StoreVO get_store_by_user_id(String userId) {
        return storeMapper.getStoreByUserId(userId);
    }

    @Override
    @Transactional
    public void addMenu(MenuVO vo, String userId) {
        StoreVO store = storeMapper.getStoreDetail(vo.getStore_id());
        if (store != null && store.getUser_id().equals(userId)) {
            if (vo.getMenu_sign() == null) vo.setMenu_sign("N");
            storeMapper.insertMenu(vo);
        }
    }

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

    @Override
    @Transactional
    public void modifyMenu(MenuVO vo, String userId) {
        MenuVO menu = storeMapper.getMenuDetail(vo.getMenu_id());
        if (menu != null) {
            StoreVO store = storeMapper.getStoreDetail(menu.getStore_id());
            if (store != null && store.getUser_id().equals(userId)) {
                if (vo.getMenu_sign() == null) vo.setMenu_sign("N");
                storeMapper.updateMenu(vo);
            }
        }
    }

    // [수정 포인트: 시간 슬롯 생성 로직 구현]
    @Override
    public List<String> generateTimeSlots(StoreVO store) {
        List<String> slots = new ArrayList<>();
        if (store == null || store.getOpen_time() == null || store.getClose_time() == null) {
            return slots;
        }

        try {
            // DB의 HH:mm:ss 또는 HH:mm 형식을 유연하게 처리
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

    // [수정 포인트: 파일 업로드 로직 구현]
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