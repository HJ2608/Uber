package com.firstapp.uber.websocket.listener;

import com.firstapp.uber.auth.CustomUserDetails;
import com.firstapp.uber.service.driver.DriverContextService;
import com.firstapp.uber.service.driver.DriverService;
import com.firstapp.uber.user.UserRepo;
import com.firstapp.uber.websocket.registry.WebSocketSessionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
public class WebSocketEventsListener {

    @Autowired
    private WebSocketSessionRegistry registry;
    @Autowired
    private DriverService driverService;
    @Autowired
    private DriverContextService contextService;
    @Autowired
    private UserRepo userRepo;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        Integer driverId = extractDriverId(event.getMessage());
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());


        Authentication auth = (Authentication) accessor.getUser();
        String principalName = accessor.getUser().getName();
        if (driverId != null) {
            String sessionId = accessor.getSessionId();
            registry.register(driverId, principalName);
        }
        System.out.println("WS Principal = " + accessor.getUser());
        System.out.println("WS Principal name = " + principalName);
        System.out.println("WS CONNECT simpSessionId=" + event.getMessage().getHeaders().get("simpSessionId"));
        System.out.println("WS CONNECT extracted driverId=" + driverId);
        System.out.println("WS CONNECT registry now online? " + registry.isOnline(driverId));
        System.out.println("simpUser = " + accessor.getUser());
        System.out.println("simpUser.name = " + accessor.getUser().getName());
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
//        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
//
//        Authentication auth = (Authentication) accessor.getUser();

//        Above this

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

//      Below this

//        return contextService.getDriverId(auth);

//        above this

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Principal principal = accessor.getUser();

        if (principal == null) return null;

        // If it's Authentication, you can access CustomUserDetails
        if (principal instanceof Authentication auth) {
            return contextService.getDriverId(auth);
        }

        // Otherwise it's handshake Principal (lambda) -> name = phone
        String phone = principal.getName();
        // If your contextService can handle phone directly, do it there.
        // Quick approach: map phone -> userId -> driverId
        Integer userId = userRepo.findUserIdByMobile(phone)
                .orElse(null);
        if(userId == null) return null;
        return driverService.findDriverIdByUserId(userId);
    }
}
