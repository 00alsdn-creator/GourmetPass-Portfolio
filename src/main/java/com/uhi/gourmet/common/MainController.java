package com.uhi.gourmet.common;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.uhi.gourmet.store.StoreMapper;
import com.uhi.gourmet.store.StoreVO;

@Controller
public class MainController {

    @Autowired
    private StoreMapper storeMapper;

    // 홈페이지 접속 시 실행되는 메서드
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String mainPage(Model model) {
        
        // [1] 인기 맛집 조회 (조회수가 높은 순서로 가져오기)
        List<StoreVO> storeList = storeMapper.selectPopularStore();
        
        // [2] 화면(main.jsp)으로 데이터 전달
        model.addAttribute("storeList", storeList);
        
        // [3] views/main.jsp 파일을 보여줌
        return "main"; 
    }
}