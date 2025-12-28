package com.firstapp.uber.auth;

public record LoginResponse(
        String accessToken,
        Integer userId,
        String firstName,
        String lastName,
        String mobileNum,
        String email,
        String role
) {
}
