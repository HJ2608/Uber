package com.firstapp.uber.auth;

public record LoginRequest(
        String mobileNum,
        String password
) {
}
