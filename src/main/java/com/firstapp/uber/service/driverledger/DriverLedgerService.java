package com.firstapp.uber.service.driverledger;

import com.firstapp.uber.dto.driverledger.DriverLedger;
import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.repository.driverledger.DriverLedgerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DriverLedgerService {

    private static final BigDecimal COMPANY_PERCENT =
            new BigDecimal("0.20");

    private static final BigDecimal DRIVER_PERCENT =
            new BigDecimal("0.80");

    private final DriverLedgerRepository ledgerRepo;

    public DriverLedgerService(DriverLedgerRepository ledgerRepo) {
        this.ledgerRepo = ledgerRepo;
    }

    @Transactional
    public void createLedgerEntry(Ride ride) {

        if (ledgerRepo.existsByRideId(ride.getRideId())) {
            return;
        }

        BigDecimal totalFare = ride.getFinalFare();

        BigDecimal companyCut =
                totalFare.multiply(COMPANY_PERCENT);

        BigDecimal driverCut =
                totalFare.multiply(DRIVER_PERCENT);

        DriverLedger ledger = new DriverLedger();
        ledger.setRideId(ride.getRideId());
        ledger.setDriverId(ride.getDriverId());
        ledger.setTotalFare(totalFare);
        ledger.setCompanyCut(companyCut);
        ledger.setDriverCut(driverCut);

        ledgerRepo.save(ledger);
    }
}

