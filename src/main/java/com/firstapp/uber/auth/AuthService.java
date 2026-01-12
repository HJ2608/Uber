package com.firstapp.uber.auth;

import com.firstapp.uber.user.UserRepo;
import model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class AuthService {

    private final UserRepo repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepo repo, PasswordEncoder encoder, JwtService jwtService) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public SignupResponse signup(SignupRequest req) {

        repo.findByMobile(req.mobileNum()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mobile already registered");
        });

        repo.findByEmail(req.email()).ifPresent(u -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        });


        String hash = encoder.encode(req.password());


        User user = new User(
                null,
                req.firstName(),
                req.lastName(),
                req.mobileNum(),
                req.email(),
                hash,
                req.role()
        );

        repo.create(user);

        User created = repo.findByMobile(req.mobileNum())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create user"));

        return new SignupResponse(
                created.id(),
                created.first_name(),
                created.last_name(),
                created.mobile_num(),
                created.email(),
                created.role()
        );
    }

    public LoginResponse login(LoginRequest req) {
        User user = repo.findByMobile(req.mobileNum())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!encoder.matches(req.password(), user.password_hash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.id(),
                user.mobile_num()
        );

        return new LoginResponse(
                token,
                user.id(),
                user.first_name(),
                user.last_name(),
                user.mobile_num(),
                user.email(),
                user.role()
        );
    }
}
