package com.firstapp.uber.service.otp;

import com.firstapp.uber.repository.otp.OtpRepo;
import com.firstapp.uber.dto.otp.Otp;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;

@Service
public class OtpServiceImpl implements OtpService{
    private final OtpRepo otpRepo;
    private final SecureRandom random = new SecureRandom();

    public OtpServiceImpl(OtpRepo otpRepo) {
        this.otpRepo = otpRepo;
    }


    private String generateNumericOtp(int digits) {
        int bound = (int) Math.pow(10, digits);  // 10^digits
        int number = random.nextInt(bound);      // 0..bound-1
        return String.format("%0" + digits + "d", number);
    }


    public Otp generateLoginOtp(Integer userId) {
        String code = generateNumericOtp(6);
        return otpRepo.createOtp(userId, code, "LOGIN", Duration.ofMinutes(5));
    }

    public boolean verifyLoginOtp(Integer userId, String otpCode) {
        Optional<Otp> match = otpRepo.findValidOtp(userId, otpCode, "LOGIN");
        if (match.isEmpty()) {
            return false;
        }
        otpRepo.markUsed(match.get().getOtpId());
        return true;
    }
    public Otp generateRideStartOtp(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("generateRideStartOtp: userId is null");
        }
        String code = generateNumericOtp(4);
        return otpRepo.createOtp(userId, code, "RIDE_START", Duration.ofMinutes(10));
    }
    public Optional<Otp> consumeRideStartOtp(Integer userId, String otpCode) {
        Optional<Otp> match = otpRepo.findValidOtp(userId, otpCode, "RIDE_START");
        if (match.isEmpty()) {
            return Optional.empty();
        }
        otpRepo.markUsed(match.get().getOtpId());
        return match;
    }

    @Override
    public Optional<Otp> getOtpById(Integer otpId) {
        return otpRepo.findById(otpId);
    }
}
