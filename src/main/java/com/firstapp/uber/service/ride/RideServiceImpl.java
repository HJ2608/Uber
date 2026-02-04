package com.firstapp.uber.service.ride;

import com.firstapp.uber.config.KafkaProducerConfig;
import com.firstapp.uber.dto.driver.Driver;
import com.firstapp.uber.dto.driver.DriverRequest;
import com.firstapp.uber.dto.driver.DriverResponse;
import com.firstapp.uber.dto.driverledger.DriverLedger;
import com.firstapp.uber.dto.driverlocation.DriverLocation;
import com.firstapp.uber.dto.otp.Otp;
import com.firstapp.uber.dto.ride.CreateRideResponse;
import com.firstapp.uber.dto.ride.PaymentSuccessEvent;
import com.firstapp.uber.dto.ride.Ride;
import com.firstapp.uber.dto.ride.RideDetail;
import com.firstapp.uber.google.DistanceMatrixResponse;
import com.firstapp.uber.repository.driver.DriverRepo;
import com.firstapp.uber.repository.driver.DriverRepository;
import com.firstapp.uber.repository.driverlocation.DriverLocationRedisRepo;
import com.firstapp.uber.repository.driverlocation.DriverLocationRepo;
import com.firstapp.uber.repository.ride.RideRepo;
import com.firstapp.uber.repository.cab.CabRepository;
import com.firstapp.uber.dto.cab.Cab;
import com.firstapp.uber.repository.ride.RideRepository;
import com.firstapp.uber.ride.dto.CabSummary;
import com.firstapp.uber.ride.dto.DriverSummary;
import com.firstapp.uber.ride.dto.EtaResponse;
import com.firstapp.uber.ride.dto.RideCardResponse;
import com.firstapp.uber.service.driver.DriverNotificationService;
import com.firstapp.uber.service.driverledger.DriverLedgerService;
import com.firstapp.uber.service.google.GoogleMapsService;
import com.firstapp.uber.service.otp.OtpService;
import com.firstapp.uber.websocket.registry.WebSocketSessionRegistry;
import jakarta.transaction.Transactional;
import model.PaymentStatus;
import model.Status;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
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
    private final DriverNotificationService notificationService;
    private final RideRepository rideRepository;
    private final RideRequestCache rideRequestCache;
    private final WebSocketSessionRegistry  webSocketSessionRegistry;
    private final DriverLedgerService  driverLedgerService;
    private final DriverLocationRedisRepo  redisRepo;
    private final DriverRepository driverRepository;
    private final PaymentProducer paymentProducer;
    private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;

    public RideServiceImpl(RideRepo rideRepo,
                           GoogleMapsService googleMapsService,
                           OtpService otpService,
                           DriverLocationRepo driverLocationRepo,
                           DriverRepo driverRepo,
                           CabRepository cabRepository,
                           DriverNotificationService notificationService ,
                           RideRepository rideRepository,
                           RideRequestCache rideRequestCache,
                           WebSocketSessionRegistry webSocketSessionRegistry,
                           DriverLedgerService driverLedgerService,
                           DriverLocationRedisRepo redisRepo,
                           DriverRepository driverRepository,
                           PaymentProducer paymentProducer,
                           KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate) {

        this.rideRepo = rideRepo;
        this.googleMapsService = googleMapsService;
        this.otpService = otpService;
        this.driverLocationRepo = driverLocationRepo;
        this.driverRepo = driverRepo;
        this.cabRepository = cabRepository;
        this.notificationService = notificationService;
        this.rideRepository = rideRepository;
        this.rideRequestCache = rideRequestCache;
        this.webSocketSessionRegistry = webSocketSessionRegistry;
        this.driverLedgerService = driverLedgerService;
        this.redisRepo = redisRepo;
        this.driverRepository = driverRepository;
        this.paymentProducer = paymentProducer;
        this.kafkaTemplate = kafkaTemplate;
    }

    private static final BigDecimal GRACE_PERCENT = new BigDecimal("0.15");
    private static final BigDecimal EXTRA_PER_MIN = new BigDecimal("5.00");


    public CreateRideResponse createRide(Integer userId,
                                         double pickupLat, double pickupLng,
                                         double dropLat, double dropLng) {

        System.out.println(
                "CREATE_RIDE req -> userId=" + userId
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

        List<Integer> candidateIds = redisRepo.findNearestWithinKm(pickupLat, pickupLng, 3.0, 10);

        List<Integer> driverIds = candidateIds.isEmpty()
                ? List.of()
                : driverRepository.filterAvailableFromCandidates(candidateIds.toArray(new Integer[0]));

        Ride ride =  rideRepo.createRide(
                userId,
                pickupLat,
                pickupLng,
                dropLat,
                dropLng,
                estimatedFare,
                rideOtp.getOtpId()
        );

        DriverRequest req = new DriverRequest(
                ride.getRideId(),
                estimatedFare,
                pickupLat,
                pickupLng,
                dropLat,
                dropLng
        );

        System.out.println("Nearby drivers found: " + driverIds);

        rideRequestCache.put(ride.getRideId(), driverIds);

        driverIds.forEach(driverId -> {
            System.out.println("Checking WS online for driverId=" + driverId
                    + " -> " + webSocketSessionRegistry.isOnline(driverId));
            if (webSocketSessionRegistry.isOnline(driverId)) {
                rideRequestCache.addSubscriber(ride.getRideId(), driverId);
                notificationService.sendRideRequest(driverId, req);
            }
        });


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

    @Override
    public List<Ride> getPendingRidesForDriver(Integer driverId) {
        // TODO: optionally filter by proximity using driver location
        return rideRepository.findPendingRides();
    }

    @Override
    public Optional<Ride> getCurrentRideForDriver(Integer driverId) {
        return rideRepository.findCurrentRideForDriver(driverId);
    }

    @Transactional
    public void acceptRide(Integer driverId, Integer rideId) {

        int updatedRows = rideRepository.assignDriverIfFree(rideId, driverId);

        if (updatedRows == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ride already taken"
            );
        }

        List<Integer> subscribedDrivers = rideRequestCache.get(rideId);

        notificationService.notifyRideTaken(
                rideId,
                driverId
        );
        rideRequestCache.remove(rideId);
    }

    public Ride handleDriverResponse(DriverResponse driverResponse, Integer driverId) {
        try {
            System.out.println("Inside try block of handleDriverResponse");
            Ride ride = assignDriver(driverResponse.rideId(), driverId);
            notificationService.notifyRideAssignment(ride, driverId);
            return ride;
        } catch (Exception e) {
            notificationService.notifyRideAssignmentFailed(driverId);
            throw e;
        }
    }

//    public List<Ride> getNearbyRides(Integer driverId) {
//        Driver driver = driverRepo.findById(driverId).orElseThrow();
//
//        // Use driver location and your existing query
//        double lat = driver.getLat();
//        double lng = driver.getLng();
//
//        List<Integer> nearbyDriverIds = rideRepo.findNearbyAvailableDrivers(lat, lng);
//
//        // Only fetch rides for this driver
//        return rideRepo.findAllPendingRidesForDrivers(nearbyDriverIds);
//    }


    public boolean startRide(Integer rideId, String otp) {

        var match = otpService.consumeRideStartOtp(rideId, otp);
        if (match.isEmpty()) {
            return false;
        }

        int updated = rideRepo.startRide(match.get().getOtpId());
        return updated > 0;
    }

    public Ride assignDriver(Integer rideId, Integer driverId) {
        System.out.println("Inside of assignDriver");
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

        if (ride.getStartedOn() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Ride not started");
        }

        LocalDateTime endedOn = LocalDateTime.now();
        ride.setEndedOn(endedOn);

        long actualSeconds =
                Duration.between(ride.getStartedOn(), endedOn).getSeconds();

        BigDecimal estimatedFare = ride.getEstimatedFare();
        if (estimatedFare == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Estimated fare missing");
        }

        BigDecimal finalFare = calculateFinalFare(
                estimatedFare,
                actualSeconds
        );



        ride.setFinalFare(finalFare);


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

        System.out.println("driverLat: " + driverLat + " driverLng: " + driverLng);
        if (distanceKm > 5.0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Driver is too far from drop point (" + String.format("%.2f", distanceKm) + " km)");
        }

        ride.setStatus(Status.COMPLETED);

        rideRepository.save(ride);

        return rideRepo.endRide(rideId);
    }

    private BigDecimal calculateFinalFare(
            BigDecimal estimatedFare,
            long actualSeconds
    ) {
        // heuristic: estimated duration ≈ fare * constant
        // for now assume avg speed & pricing already baked into fare

        long estimatedSeconds = 15 * 60; // example: 15 min default
        long graceSeconds = (long) (estimatedSeconds * 1.15);

        if (actualSeconds <= graceSeconds) {
            return estimatedFare; // no increase
        }

        long extraSeconds = actualSeconds - graceSeconds;
        long extraMinutes = (long) Math.ceil(extraSeconds / 60.0);

        BigDecimal surcharge =
                EXTRA_PER_MIN.multiply(BigDecimal.valueOf(extraMinutes));

        return estimatedFare.add(surcharge);
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

    public RideDetail getCurrentRideFromRideId(Integer rideId) {
        return rideRepository.getRideSummary(rideId);
    }

    public Ride getCurrentRide(Integer custId) {
        return rideRepo.getCurrentRide(custId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No active ride for this user"));
    }
    public Ride markPaymentSuccess(Integer rideId, String method) {
//        Ride ride = rideRepo.updatePayment(rideId, method);
//        ride.setStatus(Status.COMPLETED);
//        rideRepository.save(ride);
//
//        driverLedgerService.createLedgerEntry(ride);
//        return ride;
        Ride ride = rideRepo.findById(rideId)
                .orElseThrow (()-> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No ride with this id"));
        if (ride.getFinalFare() == null || ride.getEndedOn() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ride must be ended before payment");
        }
        paymentProducer.publishPaymentSuccess(rideId, method);
        return rideRepository.findById(rideId).orElseThrow();
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

    public Ride getActiveRideForDriver(Integer driverId) {
        return rideRepo.getCurrentRideForDriver(driverId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No active ride for this driver"));
    }

    public void publishPaymentCompleted(PaymentSuccessEvent event) {
        kafkaTemplate.send("payment-events", event.rideId().toString(), event);
    }
}
