package com.firstapp.uber.service.driver;

import com.firstapp.uber.dto.driver.DriverRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DriverSseService {
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Integer driverId){
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(driverId, emitter);

        emitter.onCompletion(() -> emitters.remove(driverId));
        emitter.onTimeout(() -> emitters.remove(driverId));
        emitter.onError((e) -> emitters.remove(driverId));

        return emitter;
    }

    public void sendRideRequest(Integer driverId, DriverRequest request){
        SseEmitter emitter = emitters.get(driverId);
        if (emitter == null) return;

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("ride-request")
                            .data(request)
            );
        } catch (Exception e) {
            emitters.remove(driverId);
        }
    }

}
