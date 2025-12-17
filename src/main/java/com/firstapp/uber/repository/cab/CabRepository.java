package com.firstapp.uber.repository.cab;

import com.firstapp.uber.dto.cab.Cab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CabRepository extends JpaRepository<Cab, Integer> {

    List<Cab> findByIsActiveTrue();
}
