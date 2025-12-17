package com.firstapp.uber.service.google;

import com.firstapp.uber.google.DistanceMatrixResponse;
import com.firstapp.uber.google.GeocodingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


public interface GoogleMapsService {


    public GeocodingResponse.Location geocode(String address);

    public String reverseGeocode(double lat, double lng);

    public DistanceMatrixResponse.Element getDistanceAndTime(
            double originLat, double originLng,
            double destLat, double destLng
    );
}