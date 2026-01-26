package com.firstapp.uber.dto.driverledger;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_ledger",
uniqueConstraints = @UniqueConstraint(columnNames = "ride_id"))
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PUBLIC)
@AllArgsConstructor
public class DriverLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="driver_id", nullable = false)
    private Integer driverId;
    @Column(name="ride_id", nullable = false)
    private Integer rideId;
    @Column(name = "total_fare", nullable = false)
    private BigDecimal totalFare;
    @Column(name="driver_cut", nullable = false)
    private BigDecimal driverCut;
    @Column(name ="company_cut", nullable = false)
    private BigDecimal companyCut;
    @CreationTimestamp
    @Column(name="created_at", updatable = false)
    private LocalDateTime createdAt;
}
