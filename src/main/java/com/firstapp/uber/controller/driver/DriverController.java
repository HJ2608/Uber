package com.firstapp.uber.controller.driver;


import com.firstapp.uber.repository.driver.DriverRepo;
import com.firstapp.uber.dto.driver.Driver;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/drivers")
public class DriverController {
    private final DriverRepo repo;

    public DriverController(DriverRepo repo){
        this.repo = repo;
    }
    @GetMapping
    public List<Driver> getAllDriver(){
        return repo.findAll();
    }
    @GetMapping("/id/{id}")
    public Driver getDriverById(@PathVariable Integer id){
        return repo.findById(id).orElseThrow();
    }
    @PostMapping()
    public void createDriver(@RequestBody Driver driver){
        repo.createDriver(driver);
    }
    @PutMapping("/id/{id}")
    public boolean updateDriver(@RequestBody Driver driver, @PathVariable Integer id){
        return repo.update(id,driver);
    }
    @DeleteMapping("/id/{id}")
    public void deleteDriver(@PathVariable Integer id){
        repo.deleteDriver(id);
    }

}
