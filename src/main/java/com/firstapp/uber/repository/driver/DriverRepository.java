package com.firstapp.uber.repository.driver;

import com.firstapp.uber.dto.driver.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {
    List<Driver> findByCabIdIn(List<Integer> cabIds);
    Optional<Driver> findByUser_Id(Integer userId);
    @Query(value = """
        SELECT d.id
        FROM drivers d
        JOIN cabs c ON c.id = d.cab_id
        LEFT JOIN rides r
            ON r.driver_id = d.id
            AND r.status IN ('ASSIGNED','ONGOING','WAITING')
        WHERE d.id = ANY(:candidateIds)
          AND c.is_active = TRUE
          AND d.is_online = 'ONLINE'
          AND r.id IS NULL
        ORDER BY array_position(:candidateIds, d.id)
        """, nativeQuery = true)
    List<Integer> filterAvailableFromCandidates(@Param("candidateIds") Integer[] candidateIds);
}
