package com.firstapp.uber.dto.ride;

import jakarta.persistence.*;
import lombok.*;
import model.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class Ride{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        Integer rideId;
        @Column(name = "cust_id")
        Integer custId;
        @Column(name = "driver_id")
        Integer driverId;
        @Column(name = "pickup_lat")
        double pickupLat;
        @Column(name = "pickupLng")
        double pickupLng;
        @Column(name = "drop_lat")
        double dropLat;
        @Column(name = "dropLng")
        double dropLng;
        @Column(name = "otp_id")
        Integer otpId;
        @Column(name = "otp_verified")
        Boolean otpVerified;
        @Column(name = "estimated_fare")
        BigDecimal estimatedFare;
        @Column(name = "final_fare")
        BigDecimal finalFare;
        @Column(name = "started_on")
        LocalDateTime startedOn;
        @Column(name = "ended_on")
        LocalDateTime endedOn;
        @Column(name = "payment_status")
        String paymentStatus;
        @Column(name = "payment_method")
        String paymentMethod;
        @Enumerated(EnumType.STRING)
        @Column(name = "status")
        Status status;
        @Column(name = "cab_id")
        Integer cabId;

}
