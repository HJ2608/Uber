package com.firstapp.uber.repository.ride;

import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.dto.ride.RideDetail;
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

    Optional<Ride> findActiveRideByDriverId(Integer driverId);


    @Modifying
    @Transactional
    @Query("""
    UPDATE Ride r
    SET r.paymentStatus = 'COMPLETED',
        r.paymentMethod = :method
    WHERE r.rideId = :rideId
    """)
    int updatePayment(Integer rideId, String method);


//    UPDATE Ride r
//    SET r.driverId = :driverId,
//    r.status = model.Status.ASSIGNED
//    WHERE r.rideId = :rideId
//    AND r.driverId IS NULL
//    AND r.status = model.Status.REQUESTED
//    AND EXISTS (
//            SELECT 1 FROM Driver d
//                    JOIN d.cab c
//                    WHERE d.id = :driverId
//                    AND d.status = model.DriverStatus.ONLINE
//                    AND c.isActive = true
//                    AND NOT EXISTS (
//                    SELECT 1 FROM Ride r2
//                    WHERE r2.driverId = d.id
//                    AND r2.status IN (
//                    model.Status.ASSIGNED,
//            model.Status.ONGOING,
//            model.Status.WAITING
//            )
//                    )
//                            )
    @Modifying
    @Query(value = """
            UPDATE rides r
                        SET r.driver_id = :driverId,
                        r.status = 'ASSIGNED'
                        WHERE r.id = :rideId
                        AND r.driver_id IS NULL
                        AND r.status = 'REQUESTED'
                        AND EXISTS (
                                SELECT 1
                                        FROM drivers d
                                        JOIN cabs c ON d.cab_id = c.id
                                        WHERE d.id = :driverId
                                        AND d.is_online = 'ONLINE'
                                        AND c.is_active = true
                                        AND NOT EXISTS (
                                        SELECT 1
                                        FROM rides r2
                                        WHERE r2.driver_id = d.id
                                        AND r2.status IN ('ASSIGNED', 'ONGOING', 'WAITING')
                                )
                        )
    """, nativeQuery = true)
    int assignDriverIfFree(@Param("rideId") Integer rideId,
                           @Param("driverId") Integer driverId);

    @Modifying
    @Transactional
    @Query("""
    UPDATE Ride r
    SET r.driverId = :driverId,
    r.status = 'ASSIGNED'
    WHERE r.rideId = :rideId
    AND r.status = 'REQUESTED'
    """)
    int tryAssignDriver(
            @Param("rideId") Integer rideId,
            @Param("driverId") Integer driverId
    );


    boolean existsByDriverIdAndStatusIn(Integer driverId, List<Status> statuses);

    @Query("""
        SELECT r FROM Ride r 
        WHERE r.status = 'REQUESTED'
        """)
    List<Ride> findPendingRides();

    @Query("""
        SELECT r FROM Ride r 
        WHERE r.driverId = :driverId AND r.status IN ('ASSIGNED', 'ONGOING')
    """)
    Optional<Ride> findCurrentRideForDriver(@Param("driverId") Integer driverId);

    @Query("""
    SELECT new com.firstapp.uber.dto.ride.RideDetail(
        r.id,
        CONCAT(u.firstName, ' ', u.lastName),
        u.mobileNum,
        r.pickupLat,
        r.pickupLng,
        r.dropLat,
        r.dropLng,
        r.estimatedFare
    )
    FROM Ride r
    JOIN UserEntity u ON r.custId = u.id
    WHERE r.id = :rideId
""")
    RideDetail getRideSummary(@Param("rideId") Integer rideId);
}
