package com.firstapp.uber.repository.ride;

import com.firstapp.uber.dto.ride.Ride;
import jakarta.transaction.Transactional;
import model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Integer> {
    @Modifying
    @Transactional
    @Query("""
        UPDATE Ride r
        SET r.otpVerified = true,
            r.status = 'ONGOING',
            r.startedOn = CURRENT_TIMESTAMP
        WHERE r.otpId = :otpId
    """)
    int startRide(@Param("otpId") Integer otpId, @Param("status") Status status);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Ride r
        SET r.status = 'COMPLETED',
            r.endedOn = CURRENT_TIMESTAMP
        WHERE r.rideId = :rideId
    """)
    int markRideCompleted(Integer rideId, @Param("status") Status status);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Ride r
        SET r.status = 'CANCELLED'
        WHERE r.rideId = :rideId
    """)
    int markRideCancelled(Integer rideId,  @Param("status") Status status);


    Optional<Ride> findTopByCustIdAndStatusInOrderByRideIdDesc(
            Integer custId,
            List<String> statuses
    );

    @Modifying
    @Transactional
    @Query("""
    UPDATE Ride r
    SET r.paymentStatus = 'SUCCESS',
        r.paymentMethod = :method
    WHERE r.rideId = :rideId
""")
    int updatePayment(Integer rideId, String method);

    boolean existsByDriverIdAndStatusIn(Integer driverId, List<Status> statuses);

}
