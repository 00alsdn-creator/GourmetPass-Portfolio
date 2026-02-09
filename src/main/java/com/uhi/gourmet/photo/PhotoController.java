package com.uhi.gourmet.photo;

import com.uhi.gourmet.store.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@Controller
@RequestMapping("/photo")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private StoreService storeService;

    @PostMapping("/upload")
    public String uploadPhotos(@RequestParam("store_id") int store_id,
                               @RequestParam("files") MultipartFile[] files,
                               Principal principal,
                               HttpServletRequest request,
                               RedirectAttributes rttr) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        if (files == null || files.length == 0) {
            rttr.addFlashAttribute("msg", "업로드할 사진을 선택해주세요.");
            return "redirect:/member/mypage";
        }

        int currentCount = photoService.getPhotoCountByStore(store_id);
        if (currentCount + files.length > 5) {
            rttr.addFlashAttribute("msg", "사진은 최대 5장까지만 등록할 수 있습니다.");
            return "redirect:/member/mypage";
        }

        String realPath = request.getSession().getServletContext().getRealPath("/resources/upload");
        boolean hasThumbnail = photoService.getThumbnailByStore(store_id) != null;
        boolean thumbnailSet = hasThumbnail;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            String savedName = storeService.uploadFile(file, realPath);
            if (savedName == null) {
                continue;
            }

            PhotoVO vo = new PhotoVO();
            vo.setStore_id(store_id);
            vo.setFile_path(savedName);
            vo.setOriginal_name(file.getOriginalFilename());
            vo.setIs_active("Y");
            vo.setSort_order(0);

            if (!thumbnailSet) {
                vo.setIs_thumbnail("Y");
                thumbnailSet = true;
            } else {
                vo.setIs_thumbnail("N");
            }

            photoService.addPhoto(vo);
        }

        rttr.addFlashAttribute("msg", "사진이 등록되었습니다.");
        return "redirect:/member/mypage";
    }

    @PostMapping("/thumbnail")
    public String setThumbnail(@RequestParam("store_id") int store_id,
                               @RequestParam("photo_id") int photo_id,
                               Principal principal,
                               RedirectAttributes rttr) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        photoService.activatePhoto(photo_id);
        photoService.setThumbnail(store_id, photo_id);
        rttr.addFlashAttribute("msg", "썸네일이 변경되었습니다.");
        return "redirect:/member/mypage";
    }

    @PostMapping("/delete")
    public String deletePhoto(@RequestParam("photo_id") int photo_id,
                              Principal principal,
                              RedirectAttributes rttr) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        photoService.deactivatePhoto(photo_id);
        rttr.addFlashAttribute("msg", "사진이 내려졌습니다.");
        return "redirect:/member/mypage";
    }

    @PostMapping("/remove")
    public String removePhoto(@RequestParam("photo_id") int photo_id,
                              Principal principal,
                              RedirectAttributes rttr) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        photoService.deletePhoto(photo_id);
        rttr.addFlashAttribute("msg", "사진이 삭제되었습니다.");
        return "redirect:/member/mypage";
    }

    @PostMapping("/activate")
    public String activatePhoto(@RequestParam("photo_id") int photo_id,
                                Principal principal,
                                RedirectAttributes rttr) {
        if (principal == null) {
            return "redirect:/member/login";
        }

        photoService.activatePhoto(photo_id);
        rttr.addFlashAttribute("msg", "사진이 다시 표시됩니다.");
        return "redirect:/member/mypage";
    }
}
