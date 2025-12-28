package com.firstapp.uber.repository.driverlocation;

import com.firstapp.uber.dto.driverlocation.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Integer> {
    @Query(value = """
        SELECT driver_id
        FROM (
            SELECT dl.driver_id,
                   (
                       6371 * acos(
                           cos(radians(:pickupLat)) * cos(radians(dl.lat)) *
                           cos(radians(dl.lng) - radians(:pickupLng)) +
                           sin(radians(:pickupLat)) * sin(radians(dl.lat))
                       )
                   ) AS distance_km
            FROM driver_locations dl
        ) AS distances
        WHERE distance_km <= :radiusKm
        ORDER BY distance_km
        LIMIT 1
        """,
            nativeQuery = true)
    List<Integer> findNearestDriverWithinRadius(
            @Param("pickupLat") double pickupLat,
            @Param("pickupLng") double pickupLng,
            @Param("radiusKm") double radiusKm
    );

    @Query(value = """
        SELECT d.id
        FROM drivers d
        JOIN cabs c ON c.id = d.cab_id
        JOIN driver_locations dl ON dl.driver_id = d.id
        LEFT JOIN rides r
            ON r.driver_id = d.id
            AND r.status IN ('ACCEPTED','STARTED')
        WHERE c.is_active = TRUE
          AND r.id IS NULL
          AND ST_DWithin(
              ST_MakePoint(dl.lng, dl.lat)::geography,
              ST_MakePoint(:pickupLng, :pickupLat)::geography,
              3000
          )
        ORDER BY ST_Distance(
              ST_MakePoint(dl.lng, dl.lat)::geography,
              ST_MakePoint(:pickupLng, :pickupLat)::geography
        )
        LIMIT 10
        """, nativeQuery = true)
    List<Integer> findNearbyAvailableDrivers(
            @Param("pickupLat") double pickupLat,
            @Param("pickupLng") double pickupLng
    );
}
