package com.firstapp.uber.repository.otp;

import com.firstapp.uber.dto.otp.Otp;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class OtpRepo {

    private final OtpRepository repo;

    public OtpRepo(OtpRepository otpRepository) {
        this.repo = otpRepository;
    }

    @Transactional
    public Otp createOtp(Integer userId, String otpCode, String purpose, Duration ttl) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(ttl);

        Otp otp = new Otp(
                null,
                userId,
                otpCode,
                purpose,
                now,
                expiresAt,
                null,
                true
        );

        return repo.save(otp);
    }

    public Optional<Otp> findValidOtp(Integer userId, String otpCode, String purpose) {
        return repo.findTopByUserIdAndOtpCodeAndPurposeAndIsValidTrueAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                userId, otpCode, purpose, LocalDateTime.now());
    }

    public Optional<Otp> findValidOtpForRide(Integer rideId, String otpCode, String purpose) {
        return repo.findValidOtpForRide(rideId, otpCode, purpose);
    }

    @Transactional
    public void markUsed(Integer otpId) {
        repo.markUsed(otpId);
    }

    public Optional<Otp> findById(Integer otpId) {
        return repo.findById(otpId);
    }


}
