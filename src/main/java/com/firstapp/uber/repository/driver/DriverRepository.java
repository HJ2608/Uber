package com.firstapp.uber.repository.driver;

import com.firstapp.uber.dto.driver.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {
    List<Driver> findByCabIdIn(List<Integer> cabIds);
    Optional<Driver> findDriverByUserId(Integer userId);
}
