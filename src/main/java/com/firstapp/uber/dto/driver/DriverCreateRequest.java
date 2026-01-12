package com.firstapp.uber.dto.driver;

public record DriverCreateRequest(
        String name,
        String licenseNo,
        Integer userId,
        Integer cabId
) {}
