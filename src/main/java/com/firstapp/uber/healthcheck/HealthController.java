package com.firstapp.uber.healthcheck;

import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/ping")
    public String ping(){
        return "OK";
    }
    @PostConstruct
    public void init(){
        System.out.println("HealthController Loaded!");
    }

}
