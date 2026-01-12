package com.firstapp.uber.websocket.listener;

import com.firstapp.uber.auth.CustomUserDetails;
import com.firstapp.uber.service.driver.DriverContextService;
import com.firstapp.uber.service.driver.DriverService;
import com.firstapp.uber.websocket.registry.WebSocketSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventsListener {

    @Autowired
    private WebSocketSessionRegistry registry;
    @Autowired
    private DriverService driverService;
    @Autowired
    private DriverContextService contextService;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        Integer driverId = extractDriverId(event.getMessage());
        if (driverId != null) {
            registry.register(driverId, event.getMessage().getHeaders().get("simpSessionId").toString());
        }
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        System.out.println("WS Principal = " + accessor.getUser());
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        System.out.println("DISCONNECT status = " + event.getCloseStatus());
        Integer driverId = extractDriverId(event.getMessage());
        if (driverId != null) {
            registry.remove(driverId);
        }
    }

    private Integer extractDriverId(Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Authentication auth = (Authentication) accessor.getUser();
//        if (auth != null && auth.getPrincipal() != null) {
//            try {
//                CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
//                if(!"DRIVER".equals(user.getRole())) {
//                    return null;
//                }
//                Integer driverId = driverService.findDriverIdByUserId(user.getUserId());
//                if (driverId == null) {
//                    System.out.println(
//                            "WS CONNECT: Driver profile not created yet for userId=" + user.getUserId()
//                    );
//                    return null;
//                }
//                return driverId;
//            } catch (NumberFormatException e) {
//                return null;
//            }
//        }
//        return null;

        return contextService.getDriverId(auth);
    }
}
