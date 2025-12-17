package com.firstapp.uber.auth;

public record SignupResponse (
        Integer userId,
        String firstName,
        String lastName,
        String mobileNum,
        String email
){ }
