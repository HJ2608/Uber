package com.firstapp.uber.repository.driverledger;

import com.firstapp.uber.dto.driverledger.DriverLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DriverLedgerRepository extends JpaRepository<DriverLedger,Long> {
    boolean existsByRideId(Integer rideId);

    List<DriverLedger> findByDriverId(Integer driverId);

    @Query("""
        SELECT COALESCE(SUM(l.driverCut), 0)
        FROM DriverLedger l
        WHERE l.driverId = :driverId
    """)
    BigDecimal totalEarnings(@Param("driverId") Integer driverId);

    @Query("""
        SELECT COALESCE(SUM(l.driverCut), 0)
        FROM DriverLedger l
        WHERE l.driverId = :driverId
          AND l.createdAt >= :startOfDay
          AND l.createdAt < :endOfDay
    """)
    BigDecimal todayEarnings(
            @Param("driverId") Integer driverId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        SELECT COALESCE(SUM(l.driverCut), 0)
        FROM DriverLedger l
        WHERE l.driverId = :driverId
          AND l.createdAt BETWEEN :from AND :to
    """)
    BigDecimal earningsBetween(
            @Param("driverId") Integer driverId,
            @Param("from")LocalDateTime from,
            @Param("to")LocalDateTime to
    );
}
