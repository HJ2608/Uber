package com.firstapp.uber.dto.driverlocation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_locations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
public class DriverLocation{
        @Id
        @Column(name = "driver_id")
        Integer driverId;
        @Column(name = "lat")
        BigDecimal lat;
        @Column(name = "lng")
        BigDecimal lng;
        @Column(name = "updated_at")
        LocalDateTime updatedAt;


}
