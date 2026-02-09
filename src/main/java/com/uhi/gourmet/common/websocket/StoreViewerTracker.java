package com.uhi.gourmet.common.websocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class StoreViewerTracker {

    private final ConcurrentHashMap<Integer, AtomicInteger> storeCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<Integer>> sessionStores = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        String sessionId = accessor.getSessionId();
        if (destination == null || sessionId == null) {
            return;
        }

        Integer storeId = extractStoreId(destination);
        if (storeId == null) {
            return;
        }

        sessionStores.computeIfAbsent(sessionId, key -> ConcurrentHashMap.newKeySet());
        Set<Integer> stores = sessionStores.get(sessionId);
        if (stores.add(storeId)) {
            storeCounts.computeIfAbsent(storeId, key -> new AtomicInteger(0)).incrementAndGet();
            publishCount(storeId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = StompHeaderAccessor.wrap(event.getMessage()).getSessionId();
        if (sessionId == null) {
            return;
        }

        Set<Integer> stores = sessionStores.remove(sessionId);
        if (stores == null) {
            return;
        }

        for (Integer storeId : stores) {
            AtomicInteger count = storeCounts.get(storeId);
            if (count == null) {
                continue;
            }
            int updated = count.decrementAndGet();
            if (updated <= 0) {
                storeCounts.remove(storeId);
                updated = 0;
            }
            publishCount(storeId);
        }
    }

    private Integer extractStoreId(String destination) {
        // Expected: /topic/store/{storeId}/viewers
        String prefix = "/topic/store/";
        String suffix = "/viewers";
        if (!destination.startsWith(prefix) || !destination.endsWith(suffix)) {
            return null;
        }
        String idPart = destination.substring(prefix.length(), destination.length() - suffix.length());
        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void publishCount(int storeId) {
        int count = storeCounts.getOrDefault(storeId, new AtomicInteger(0)).get();
        messagingTemplate.convertAndSend("/topic/store/" + storeId + "/viewers", count);
    }
}
