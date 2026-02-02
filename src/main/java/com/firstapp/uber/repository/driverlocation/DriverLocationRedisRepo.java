package com.firstapp.uber.repository.driverlocation;

import com.firstapp.uber.dto.driverlocation.DriverLocation;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DriverLocationRedisRepo {

    private static final String GEO_KEY = "geo:drivers";
    private static final String DIRTY_SET = "dirty:drivers";
    private static final String ONLINE_SET = "drivers:online";

    private final StringRedisTemplate redis;

    public DriverLocationRedisRepo(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void upsertLocation(Integer driverId, double lat, double lng) {
        System.out.println("REDIS UPSERT driverId=" + driverId + " lat=" + lat + " lng=" + lng);
        String member = "driver:" + driverId;
        String hashKey = "driver:loc:" + driverId;


        redis.opsForGeo().add(GEO_KEY, new Point(lng, lat), member);

        redis.opsForHash().put(hashKey, "lat", String.valueOf(lat));
        redis.opsForHash().put(hashKey, "lng", String.valueOf(lng));
        redis.opsForHash().put(hashKey, "ts", String.valueOf(System.currentTimeMillis()));


        redis.expire(hashKey, Duration.ofSeconds(180));


        redis.opsForSet().add(DIRTY_SET, driverId.toString());
    }

    public Optional<DriverLocation> getLocation(Integer driverId) {
        String hashKey = "driver:loc:" + driverId;
        Map<Object,Object> m = redis.opsForHash().entries(hashKey);
        if (m == null || m.isEmpty()) return Optional.empty();

        double lat = Double.parseDouble((String)m.get("lat"));
        double lng = Double.parseDouble((String)m.get("lng"));
        long ts = Long.parseLong((String)m.get("ts"));

        DriverLocation dl = new DriverLocation(
                driverId,
                BigDecimal.valueOf(lat),
                BigDecimal.valueOf(lng),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneOffset.UTC)
        );
        return Optional.of(dl);
    }

    public List<Integer> findNearestWithinKm(double pickupLat, double pickupLng, double radiusKm, int limit) {
        var circle = new Circle(new Point(pickupLng, pickupLat), new Distance(radiusKm, Metrics.KILOMETERS));

        var args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending()
                .limit(limit);

        GeoResults<RedisGeoCommands.GeoLocation<String>> res =
                redis.opsForGeo().radius(GEO_KEY, circle, args);

        if (res == null) return List.of();


        return res.getContent().stream()
                .map(r -> r.getContent().getName())
                .map(name -> Integer.parseInt(name.replace("driver:", "")))
                .toList();
    }


    public void markOnline(Integer driverId) {
        redis.opsForSet().add(ONLINE_SET, driverId.toString());
    }
    public void markOffline(Integer driverId) {
        redis.opsForSet().remove(ONLINE_SET, driverId.toString());
        redis.opsForGeo().remove(GEO_KEY, "driver:" + driverId);
    }
}

