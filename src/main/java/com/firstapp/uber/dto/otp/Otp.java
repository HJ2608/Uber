package com.firstapp.uber.dto.otp;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "otps")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class Otp{

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        Integer otpId;
        @Column(name = "user_id")
        Integer userId;
        @Column(name = "otpCode")
        String otpCode;
        @Column(name = "purpose")
        String purpose;
        @Column(name = "created_at")
        LocalDateTime createdAt;
        @Column(name = "expires_at")
        LocalDateTime expiresAt;
        @Column(name = "used_at")
        LocalDateTime usedAt;
        @Column(name = "is_valid")
        Boolean isValid;

}
