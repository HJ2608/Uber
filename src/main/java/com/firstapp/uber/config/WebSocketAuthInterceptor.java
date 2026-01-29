package com.firstapp.uber.config;

import com.firstapp.uber.auth.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        System.out.println("🔥 WebSocketAuthInterceptor HIT");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        System.out.println("STOMP CMD = " + accessor.getCommand());
        System.out.println("HEADERS = " + accessor.toNativeHeaderMap());

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            System.out.println("Inside WebSocketAuthInterceptor CONNECT");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);
                System.out.println("Inside WebSocketAuthInterceptor AUTH");
                Authentication auth = jwtService.authenticate(token);
                System.out.println("WS AUTH principal name = [" + auth.getName() + "]");
                accessor.setUser(auth);
                System.out.println("WS CONNECT Authorization = " +
                        accessor.getFirstNativeHeader("Authorization"));

                System.out.println("Message that is being returned: "+ MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders()));
                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());

            }
        }

        return message;
    }
}
