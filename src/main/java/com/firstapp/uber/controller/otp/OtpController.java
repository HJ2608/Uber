package com.firstapp.uber.controller.otp;

import com.firstapp.uber.service.otp.OtpService;
import com.firstapp.uber.dto.otp.Otp;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class OtpController {

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    public record SendOtpRequest(Integer user_id) {}
    public record SendOtpResponse(Integer otp_id, String otp_code, String purpose) {}

    @PostMapping("/send-login-otp")
    public SendOtpResponse sendLoginOtp(@RequestBody SendOtpRequest req) {
        Otp otp = otpService.generateLoginOtp(req.user_id());
        return new SendOtpResponse(otp.getOtpId(), otp.getOtpCode(), otp.getPurpose());
    }

    public record VerifyOtpRequest(Integer user_id, String otp_code) {}

    @PostMapping("/verify-login-otp")
    public String verifyLoginOtp(@RequestBody VerifyOtpRequest req) {
        boolean ok = otpService.verifyLoginOtp(req.user_id(), req.otp_code());
        if (!ok) {
            return "Invalid or expired OTP";
        }
        return "OTP verified, login success";
    }
}
