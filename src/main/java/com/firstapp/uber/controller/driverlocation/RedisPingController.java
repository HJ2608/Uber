package com.firstapp.uber.controller.driverlocation;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RedisPingController {
    private final StringRedisTemplate redis;

    RedisPingController(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @GetMapping("/redis-ping")
    public String ping() {
        redis.opsForValue().set("ping", "pong");
        return redis.opsForValue().get("ping");
    }
}
