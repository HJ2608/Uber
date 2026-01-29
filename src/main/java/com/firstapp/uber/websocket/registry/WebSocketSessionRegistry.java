package com.firstapp.uber.websocket.registry;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<Integer, String> driverIdToPrincipalName = new ConcurrentHashMap<>();

    public void register(Integer driverId, String principalName) {
        driverIdToPrincipalName.put(driverId, principalName);
    }

    public void remove(Integer driverId) {
        driverIdToPrincipalName.remove(driverId);
    }

    public boolean isOnline(Integer driverId) {
        return driverIdToPrincipalName.containsKey(driverId);
    }

    public String principalName(Integer driverId) {
        return driverIdToPrincipalName.get(driverId);
    }

}
