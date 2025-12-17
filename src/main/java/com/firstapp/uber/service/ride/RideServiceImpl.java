package com.firstapp.uber.service.ride;

import com.firstapp.uber.dto.driver.Driver;
import com.firstapp.uber.dto.driverlocation.DriverLocation;
import com.firstapp.uber.dto.otp.Otp;
import com.firstapp.uber.dto.ride.CreateRideRequest;
import com.firstapp.uber.dto.ride.CreateRideResponse;
import com.firstapp.uber.dto.ride.DriverRequest;
import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.google.DistanceMatrixResponse;
import com.firstapp.uber.repository.driver.DriverRepo;
import com.firstapp.uber.repository.driverlocation.DriverLocationRepo;
import com.firstapp.uber.repository.ride.RideRepo;
import com.firstapp.uber.repository.cab.CabRepository;
import com.firstapp.uber.dto.cab.Cab;
import com.firstapp.uber.ride.dto.CabSummary;
import com.firstapp.uber.ride.dto.DriverSummary;
import com.firstapp.uber.ride.dto.EtaResponse;
import com.firstapp.uber.ride.dto.RideCardResponse;
import com.firstapp.uber.service.google.GoogleMapsService;
import com.firstapp.uber.service.otp.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.List;

@Service
public class RideServiceImpl implements RideService{
    private final RideRepo rideRepo;
    private final GoogleMapsService googleMapsService;
    private final OtpService otpService;
    private final DriverLocationRepo driverLocationRepo;
    private final DriverRepo driverRepo;
    private final CabRepository cabRepository;

    public RideServiceImpl(RideRepo rideRepo,
                       GoogleMapsService googleMapsService,
                       OtpService otpService,
                       DriverLocationRepo driverLocationRepo,
                       DriverRepo driverRepo,
                       CabRepository cabRepository) {
        this.rideRepo = rideRepo;
        this.googleMapsService = googleMapsService;
        this.otpService = otpService;
        this.driverLocationRepo = driverLocationRepo;
        this.driverRepo = driverRepo;
        this.cabRepository = cabRepository;
    }

    public CreateRideResponse createRide(Integer userId,
                                         double pickupLat, double pickupLng,
                                         double dropLat, double dropLng) {

        System.out.println(
                "CREATE_RIDE req -> riderId=" + userId
                        + ", pickup=(" + pickupLat + "," + pickupLng + ")"
                        + ", drop=(" + dropLat + "," + dropLng + ")"
        );

        DistanceMatrixResponse.Element elem = googleMapsService.getDistanceAndTime(
                pickupLat,
                pickupLng,
                dropLat,
                dropLng
        );

        long distanceMeters = elem.distance.value;
        long durationSeconds = elem.duration.value;

        BigDecimal estimatedFare = calculateFare(distanceMeters, durationSeconds);
        Integer custId = userId;
        Otp rideOtp = otpService.generateRideStartOtp(custId);

        Ride ride =  rideRepo.createRide(
                userId,
                pickupLat,
                pickupLng,
                dropLat,
                dropLng,
                estimatedFare,
                rideOtp.getOtpId()
        );

        //DriverRequest driverReq = rideRepo.sendReq()

        return new CreateRideResponse(
                ride.getRideId(),
                ride.getEstimatedFare(),
                rideOtp.getOtpCode()
        );
    }
    private BigDecimal calculateFare(long distanceMeters, long durationSeconds) {
        BigDecimal baseFare = new BigDecimal("30.00");
        BigDecimal perKm = new BigDecimal("10.00");
        BigDecimal perMin = new BigDecimal("2.00");

        BigDecimal distanceKm = BigDecimal.valueOf(distanceMeters)
                .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
        BigDecimal minutes = BigDecimal.valueOf(durationSeconds)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);

        return baseFare
                .add(perKm.multiply(distanceKm))
                .add(perMin.multiply(minutes));
    }
    public boolean startRide(Integer userId, String otpCode) {

        var match = otpService.consumeRideStartOtp(userId, otpCode);
        if (match.isEmpty()) {
            return false;
        }

        int updated = rideRepo.startRide(match.get().getOtpId());
        return updated > 0;
    }

    public Ride assignDriver(Integer rideId, Integer driverId) {
        Optional<Ride> existing = rideRepo.findById(rideId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Ride not found: " + rideId);
        }
        var driverOpt = driverRepo.findById(driverId);
        if (driverOpt.isEmpty()) {
            throw new IllegalArgumentException("Driver not found: " + driverId);
        }
        Integer cabId = driverOpt.get().getCabId();
        Ride updated = rideRepo.assignDriver(rideId, driverId);
        updated.setCabId(cabId);
        return rideRepo.findById(rideId).orElse(updated);
    }

    public Ride endRide(Integer rideId) {
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ride not found"));

        if (!"SUCCESS".equalsIgnoreCase(ride.getPaymentStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment not completed");
        }

        if (ride.getDriverId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No driver assigned to this ride");
        }


        DriverLocation loc = driverLocationRepo.findDriverLocationById(ride.getDriverId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Driver location not available"));

        double driverLat = loc.getLat().doubleValue();
        double driverLng = loc.getLng().doubleValue();
        double dropLat = ride.getDropLat();
        double dropLng = ride.getDropLng();

        double distanceKm = haversineKm(driverLat, driverLng, dropLat, dropLng);

        if (distanceKm > 5.0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Driver is too far from drop point (" + String.format("%.2f", distanceKm) + " km)");
        }


        return rideRepo.endRide(rideId);
    }

    public EtaResponse getEta(Integer rideId) {
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ride not found"));

        if (ride.getDriverId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No driver assigned to this ride");
        }

        DriverLocation loc = driverLocationRepo.findDriverLocationById(ride.getDriverId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Driver location not available"));

        double driverLat = loc.getLat().doubleValue();
        double driverLng = loc.getLng().doubleValue();
        double dropLat = ride.getDropLat();
        double dropLng = ride.getDropLng();

        double distanceKm = haversineKm(driverLat, driverLng, dropLat, dropLng);

        int etaMinutes = (int) Math.ceil((distanceKm / 30.0) * 60.0);

        return new EtaResponse(distanceKm, etaMinutes);
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public Ride cancelRide(Integer rideId) {
        Optional<Ride> existing = rideRepo.findById(rideId);
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Ride not found: " + rideId);
        }

        return rideRepo.cancelRide(rideId);
    }

    public Ride getCurrentRide(Integer custId) {
        return rideRepo.getCurrentRide(custId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No active ride for this user"));
    }
    public Ride markPaymentSuccess(Integer rideId, String method) {
        return rideRepo.updatePayment(rideId, method);
    }

    public RideCardResponse getRideCard(Integer rideId) {
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ride not found: " + rideId));

        DriverSummary driverSummary = null;
        CabSummary cabSummary = null;
        EtaResponse eta = null;
        String otpCode = null;
        if (ride.getOtpId() != null) {
            Otp otp = otpService.getOtpById(ride.getOtpId()).orElse(null);
            if (otp != null) {
                otpCode = otp.getOtpCode();
            }
        }


        if (ride.getDriverId() != null) {
            Driver driver = driverRepo.findById(ride.getDriverId()).orElse(null);
            Cab cab = (driver != null && driver.getCabId() != null)
                ? cabRepository.findById(driver.getCabId()).orElse(null)
                : null;
            driverSummary = new DriverSummary(
                driver.getId(),
                driver.getName(),
                driver.getAvgRating()
            );
            if (cab != null) {
                cabSummary = new CabSummary(
                        cab.getId(),
                        cab.getModel(),
                        cab.getColor(),
                        cab.getRegistrationNo(),
                        cab.getCabType()
                );
            }
            eta = getEta(ride.getRideId());
        }
        return new RideCardResponse(
                ride.getRideId(),
                ride.getStatus(),
                ride.getEstimatedFare(),
                ride.getFinalFare(),
                ride.getPaymentStatus(),
                ride.getPaymentMethod(),
                ride.getPickupLat(),
                ride.getPickupLng(),
                ride.getDropLat(),
                ride.getDropLng(),
                otpCode,
                driverSummary,
                cabSummary,
                eta
        );
    }

    public List<Ride> getAllRides() {
        return rideRepo.findAll();
    }
}
