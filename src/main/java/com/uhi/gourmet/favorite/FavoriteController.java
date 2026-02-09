package com.uhi.gourmet.favorite;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(@RequestParam("store_id") int store_id,
                                                         Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) {
            result.put("login", false);
            result.put("favorite", false);
            result.put("count", favoriteService.getFavoriteCountByStore(store_id));
            return ResponseEntity.ok(result);
        }

        boolean isFavorite = favoriteService.isFavorite(principal.getName(), store_id);
        int count = favoriteService.getFavoriteCountByStore(store_id);
        result.put("login", true);
        result.put("favorite", isFavorite);
        result.put("count", count);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getFavorites(Principal principal) {
        Map<String, Object> result = new HashMap<>();
        if (principal == null) {
            result.put("login", false);
            result.put("storeIds", Collections.emptyList());
            return ResponseEntity.ok(result);
        }

        List<Integer> storeIds = favoriteService.getFavoritesByUser(principal.getName())
            .stream()
            .map(FavoriteVO::getStore_id)
            .collect(Collectors.toList());

        result.put("login", true);
        result.put("storeIds", storeIds);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@RequestParam("store_id") int store_id,
                                                      Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String userId = principal.getName();
        boolean isFavorite = favoriteService.isFavorite(userId, store_id);
        boolean nowFavorite;

        if (isFavorite) {
            favoriteService.removeFavorite(userId, store_id);
            nowFavorite = false;
        } else {
            favoriteService.addFavorite(userId, store_id);
            nowFavorite = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("favorite", nowFavorite);
        result.put("count", favoriteService.getFavoriteCountByStore(store_id));
        return ResponseEntity.ok(result);
    }
}
