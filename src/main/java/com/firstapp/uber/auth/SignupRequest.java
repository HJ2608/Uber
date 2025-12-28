package com.firstapp.uber.auth;

public record SignupRequest(
        String firstName,
        String lastName,
        String mobileNum,
        String email,
        String password,
        String role
) {
}
