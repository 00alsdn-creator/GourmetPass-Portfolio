package com.uhi.gourmet.common;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.uhi.gourmet.photo.PhotoService;
import com.uhi.gourmet.photo.PhotoVO;
import com.uhi.gourmet.store.StoreService; 
import com.uhi.gourmet.store.StoreVO;

/**
 * 프로젝트의 메인 관문을 담당하는 컨트롤러입니다.
 */
@Controller
public class MainController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private PhotoService photoService;

    /**
     * 홈페이지 접속 시 실행되는 메서드입니다.
     * [DTO 활용] 메인 페이지는 검색 조건이 없는 대신, DB에서 조회수 기준 상위 매장을 로드합니다.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String mainPage(Model model) {
        
        // [1] 인기 맛집 조회 
        // 서비스 계층에 새로 추가한 getPopularStores 메서드를 호출합니다.
        // 이는 StoreMapper.xml의 selectPopularStore SQL을 실행하여 상위 6개를 가져옵니다.
        List<StoreVO> storeList = storeService.getPopularStores();

        for (StoreVO store : storeList) {
            PhotoVO thumbnail = photoService.getThumbnailByStore(store.getStore_id());
            if (thumbnail != null) {
                store.setStore_img(thumbnail.getFile_path());
            }
        }
        
        // [2] 화면(main.jsp)으로 데이터 전달
        model.addAttribute("storeList", storeList);
        
        // [3] views/main.jsp 파일을 호출합니다.
        return "main"; 
    }
}
