package com.firstapp.uber.repository.driver;


import com.firstapp.uber.dto.cab.Cab;
import com.firstapp.uber.dto.driver.Driver;
import com.firstapp.uber.dto.driver.DriverCreateRequest;
import com.firstapp.uber.dto.user.UserEntity;
import com.firstapp.uber.repository.cab.CabRepository;
import com.firstapp.uber.user.UserRepo;
import jakarta.transaction.Transactional;
import model.DriverStatus;
import model.User;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public class DriverRepo {
    private final DriverRepository driverRepository;
    private final UserRepo userRepo;
    private final CabRepository cabRepository;
    public DriverRepo(DriverRepository driverRepository,
                      UserRepo userRepo,
                      CabRepository cabRepository) {
        this.driverRepository = driverRepository;
        this.userRepo = userRepo;
        this.cabRepository = cabRepository;
    }
    public List<Driver> findAll(){
        return driverRepository.findAll();
    }

    public Optional<Driver> findById(Integer id){
        return driverRepository.findById(id);
    }

    public boolean createDriver(DriverCreateRequest req){
        User user = userRepo.findById(req.userId().longValue())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserEntity userEntity = new UserEntity();
        userEntity.setId(user.id());
        userEntity.setFirstName(user.first_name());
        userEntity.setLastName(user.last_name());
        userEntity.setEmail(user.email());
        userEntity.setMobileNum(user.mobile_num());
        userEntity.setRole(user.role());
        if (!"DRIVER".equals(userEntity.getRole())) {
            throw new RuntimeException("User is not DRIVER");
        }

        Cab cab = cabRepository.findById(req.cabId())
                .orElseThrow(() -> new RuntimeException("Cab not found"));

        Driver driver = new Driver();
        driver.setName(req.name());
        driver.setLicenseNo(req.licenseNo());
        driver.setUser(userEntity);
        driver.setCabId(cab.getId()); // (OK since you kept cabId as Integer)
        driver.setIsOnline(DriverStatus.OFFLINE);
        driver.setAvgRating(0.0);
        driver.setRatingCount(0);

        driverRepository.save(driver);
        return true;
    }
    public boolean update(Integer id,Driver driver){
        if (!driverRepository.existsById(id)) return false;
        Driver updatedDriver = new Driver(
            id,
            driver.getName(),
            driver.getLicenseNo(),
            driver.getCabId(),
            driver.getAvgRating(),
            driver.getRatingCount(),
            driver.getIsOnline(),
            driver.getUser()
        );
        driverRepository.save(updatedDriver);
        return true;
    }

    public boolean deleteDriver(Integer id){
        if(!driverRepository.existsById(id)) return false;

        driverRepository.deleteById(id);
        return true;
    }

    @Transactional
    public void setOnline(Integer driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        driver.setIsOnline(DriverStatus.ONLINE);
    }

    @Transactional
    public void setOffline(Integer driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        driver.setIsOnline(DriverStatus.OFFLINE);
    }

    @Transactional
    public void updateDriverStatus(Integer driverId, DriverStatus isOnline) {
        Driver driver = findById(driverId).orElseThrow(() ->
                new RuntimeException("Driver not found with id " + driverId)
        );
        driver.setIsOnline(isOnline);
        driverRepository.save(driver);
    }

}
