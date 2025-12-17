package com.firstapp.uber.service.google;

import com.firstapp.uber.google.DistanceMatrixResponse;
import com.firstapp.uber.google.GeocodingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
public class GoogleMapsServiceImpl implements GoogleMapsService {
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public GoogleMapsServiceImpl(RestTemplate restTemplate,
                             @Value("${google.maps.api.key}") String apiKey,
                             @Value("${google.maps.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    // 1) Address -> lat/lng
    public GeocodingResponse.Location geocode(String address) {
//        String cleaned = address == null ? "" : address.trim(); // removes \n, spaces, etc.
//
//        System.out.println("Raw address = '" + address + "'");
//        System.out.println("Cleaned address = '" + cleaned + "'");
//
//        String url = UriComponentsBuilder
//                .fromHttpUrl(baseUrl + "/geocode/json")
//                .queryParam("address", cleaned)
//                .queryParam("key", apiKey)
//                .toUriString();
//
//        System.out.println("Calling Google Geocode URL: " + url);
//
//        // 👇 TEMP: get raw JSON as String
//        String json = restTemplate.getForObject(url, String.class);
//        System.out.println("RAW GEOCODE JSON = " + json);
//
//        // 👇 Then still parse into our DTO
//        GeocodingResponse response =
//                new org.springframework.web.client.RestTemplate().getForObject(url, GeocodingResponse.class);
//
//        if (response == null) {
//            throw new RuntimeException("Geocode: response is null");
//        }
//
//        System.out.println("Google Geocode status = " + response.status);
//        System.out.println("Google Geocode results size = " +
//                (response.results == null ? 0 : response.results.size()));
//
//        if (!"OK".equals(response.status)) {
//            // This will show you the actual error from Google in the logs
//            throw new RuntimeException("Geocode failed. Status=" + response.status);
//        }
//
//        if (response.results == null || response.results.isEmpty()) {
//            throw new RuntimeException("Geocode succeeded but no results for: " + address);
//        }
//
//        return response.results.get(0).geometry.location;
        GeocodingResponse.Location loc = new GeocodingResponse.Location();
        // Hard-code New Delhi coords for now
        loc.lat = 28.6139298;
        loc.lng = 77.2088282;
        return loc;

    }

    // 2) Lat/Lng -> formatted address
    public String reverseGeocode(double lat, double lng) {
        String latlng = lat + "," + lng;

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/geocode/json")
                .queryParam("latlng", latlng)
                .queryParam("key", apiKey)
                .toUriString();

        GeocodingResponse response =
                restTemplate.getForObject(url, GeocodingResponse.class);

        if (response == null || !"OK".equals(response.status) || response.results.isEmpty()) {
            throw new RuntimeException("Failed to reverse geocode: " + latlng);
        }

        return response.results.get(0).formatted_address;
    }

    // 3) Distance & ETA between two lat/lng points
    public DistanceMatrixResponse.Element getDistanceAndTime(
            double originLat, double originLng,
            double destLat, double destLng
    ) {
        String origins = originLat + "," + originLng;
        String destinations = destLat + "," + destLng;

        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/distancematrix/json")
                .queryParam("origins", origins)
                .queryParam("destinations", destinations)
                .queryParam("key", apiKey)
                .toUriString();

        DistanceMatrixResponse response =
                restTemplate.getForObject(url, DistanceMatrixResponse.class);

        if (response == null
                || !"OK".equals(response.status)
                || response.rows == null
                || response.rows.isEmpty()
                || response.rows.get(0).elements == null
                || response.rows.get(0).elements.isEmpty()) {
            throw new RuntimeException("Failed to get distance matrix");
        }

        DistanceMatrixResponse.Element element = response.rows.get(0).elements.get(0);
        if (!"OK".equals(element.status)) {
            throw new RuntimeException("Element status not OK: " + element.status);
        }

        return element;
    }
}
