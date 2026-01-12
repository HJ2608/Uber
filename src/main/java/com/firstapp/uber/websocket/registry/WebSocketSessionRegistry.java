package com.firstapp.uber.websocket.registry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<Integer, String> driverToSession = new ConcurrentHashMap<>();

    public void register(Integer driverId, String sessionId) {
        driverToSession.put(driverId, sessionId);
    }

    public void remove(Integer driverId) {
        driverToSession.remove(driverId);
    }

    public boolean isOnline(Integer driverId) {
        return driverToSession.containsKey(driverId);
    }
}
