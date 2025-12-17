package com.firstapp.uber.auth;


import com.firstapp.uber.user.UserRepo;
import model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@RequestBody SignupRequest req) {
        return service.signup(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return service.login(req);
    }
}
