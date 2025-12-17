package com.firstapp.uber.repository.otp;

import com.firstapp.uber.dto.driverlocation.DriverLocation;
import com.firstapp.uber.dto.otp.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface OtpRepository extends JpaRepository<Otp, Integer>{
    Optional<Otp> findTopByUserIdAndOtpCodeAndPurposeAndIsValidTrueAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            Integer userId,
            String otpCode,
            String purpose,
            LocalDateTime now
    );

    @Modifying
    @Query("update Otp o set o.usedAt = CURRENT_TIMESTAMP, o.isValid = false where o.id = :otpId")
    int markUsed(@Param("otpId") Integer otpId);
}
