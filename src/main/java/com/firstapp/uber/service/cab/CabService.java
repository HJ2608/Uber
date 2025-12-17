package com.firstapp.uber.service.cab;

import com.firstapp.uber.dto.cab.Cab;
import java.util.List;

public interface CabService {
    Cab saveCab(Cab cab);
    Cab getCabById(Integer cabId);
    List<Cab> getAllCabs();
    Cab updateCab(Cab cab);
    void deleteCab(Integer cabId);
    boolean assignDriverToCab(Integer cabId, Integer driverId);
}
