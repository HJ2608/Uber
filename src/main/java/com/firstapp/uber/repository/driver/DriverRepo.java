package com.firstapp.uber.repository.driver;


import com.firstapp.uber.dto.driver.Driver;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public class DriverRepo {
    private final DriverRepository driverRepository;
    public DriverRepo(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }
    public List<Driver> findAll(){
        return driverRepository.findAll();
    }

    public Optional<Driver> findById(Integer id){
        return driverRepository.findById(id);
    }

    public boolean createDriver(Driver driver){
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
            driver.getRatingCount()
        );
        driverRepository.save(updatedDriver);
        return true;
    }

    public boolean deleteDriver(Integer id){
        if(!driverRepository.existsById(id)) return false;

        driverRepository.deleteById(id);
        return true;
    }
}
