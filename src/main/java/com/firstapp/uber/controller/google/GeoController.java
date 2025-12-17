package com.firstapp.uber.controller.google;

import com.firstapp.uber.service.google.GoogleMapsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/geo")
public class GeoController {

    private final GoogleMapsService googleMapsService;

    public GeoController(GoogleMapsService googleMapsService) {
        this.googleMapsService = googleMapsService;
    }

    @GetMapping("/geocode")
    public GeocodeResponse geocode(@RequestParam String address) {
        var loc = googleMapsService.geocode(address);
        return new GeocodeResponse(loc.lat, loc.lng);
    }

    @GetMapping("/reverse-geocode")
    public ReverseGeocodeResponse reverseGeocode(@RequestParam double lat,
                                                 @RequestParam double lng) {
        String address = googleMapsService.reverseGeocode(lat, lng);
        return new ReverseGeocodeResponse(address);
    }

    @GetMapping("/distance")
    public DistanceResponse distance(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destLat,
            @RequestParam double destLng
    ) {
        var elem = googleMapsService.getDistanceAndTime(originLat, originLng, destLat, destLng);
        return new DistanceResponse(
                elem.distance.text,
                elem.distance.value,
                elem.duration.text,
                elem.duration.value
        );
    }

    public record GeocodeResponse(double lat, double lng) {}
    public record ReverseGeocodeResponse(String address) {}
    public record DistanceResponse(
            String distanceText,
            long distanceMeters,
            String durationText,
            long durationSeconds
    ) {}
}
