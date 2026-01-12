package com.firstapp.uber.controller.driver;

import com.firstapp.uber.auth.CustomUserDetails;
import com.firstapp.uber.dto.driverledger.DriverLedger;
import com.firstapp.uber.repository.driverledger.DriverLedgerRepository;
import com.firstapp.uber.service.driver.DriverContextService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/driver/earnings")
public class DriverEarningsController {
    private final DriverLedgerRepository ledgerRepo;
    private final DriverContextService driverContextService;

    public DriverEarningsController(DriverLedgerRepository ledgerRepo,
                                    DriverContextService driverContextService) {
        this.ledgerRepo = ledgerRepo;
        this.driverContextService = driverContextService;
    }

    @GetMapping("/total")
    public BigDecimal total(Authentication auth) {
        Integer driverId = driverContextService.getDriverId(auth);
        return ledgerRepo.totalEarnings(driverId);
    }

    @GetMapping("/today")
    public BigDecimal today(Authentication auth) {
        Integer driverId = driverContextService.getDriverId(auth);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();


        return ledgerRepo.todayEarnings(driverId, start, end);
    }

//    @GetMapping("/rides")
//    public List<DriverLedger> rides(Principal principal) {
//        Integer driverId = extractDriverId(principal);
//        return ledgerRepo.findByDriverIdOrderByCreatedAtDesc(driverId);
//    }

}
