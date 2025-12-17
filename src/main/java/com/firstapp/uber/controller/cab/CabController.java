package com.firstapp.uber.controller.cab;

import com.firstapp.uber.dto.cab.AssignDriverRequest;
import com.firstapp.uber.dto.cab.Cab;
import com.firstapp.uber.service.cab.CabService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cabs")
public class CabController {
    private final CabService cabService;

    public CabController(CabService cabService) {
        this.cabService = cabService;
    }

    @PostMapping
    public Cab createCab(@RequestBody Cab cab) {
        return cabService.saveCab(cab);
    }

    @GetMapping("/{id}")
    public Cab getCab(@PathVariable Integer id) {
        return cabService.getCabById(id);
    }

    @GetMapping
    public List<Cab> getAllCabs() {
        return cabService.getAllCabs();
    }

    @PutMapping("/{id}")
    public Cab updateCab(@PathVariable Integer id, @RequestBody Cab cab) {
        cab.setId(id);
        return cabService.updateCab(cab);
    }

    @DeleteMapping("/{id}")
    public void deleteCab(@PathVariable Integer id) {
        cabService.deleteCab(id);
    }

    @PostMapping("/assign-driver")
    public String assignDriverToCab(@RequestBody AssignDriverRequest request) {
        boolean success = cabService.assignDriverToCab(request.getCabId(), request.getDriverId());
        return success ? "Driver assigned successfully" : "Assignment failed";
    }
}
