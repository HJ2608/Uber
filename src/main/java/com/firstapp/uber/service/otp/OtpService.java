package com.firstapp.uber.service.otp;

import com.firstapp.uber.dto.otp.Otp;

import java.util.Optional;


public interface OtpService {

    public Otp generateLoginOtp(Integer userId);

    public boolean verifyLoginOtp(Integer userId, String otpCode);
    public Otp generateRideStartOtp(Integer userId);

    public Optional<Otp> consumeRideStartOtp(Integer rideId, String otp);

    public Optional<Otp> getOtpById(Integer otpId);
}
