package com.firstapp.uber.job;

import com.firstapp.uber.repository.driverlocation.DriverLocationRepo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class DriverLocationFlushJob {

    private final StringRedisTemplate redis;
    private final DriverLocationRepo driverLocationRepo;

    private static final String DIRTY_SET = "dirty:drivers";

    public DriverLocationFlushJob(StringRedisTemplate redis,
                                  DriverLocationRepo driverLocationRepo) {
        this.redis = redis;
        this.driverLocationRepo = driverLocationRepo;
    }

    @Scheduled(fixedRate = 10 * 1000)
    public void flushLocationsToDb() {
        for (int i = 0; i < 5000; i++) { // safety bound
            String idStr = redis.opsForSet().pop(DIRTY_SET);
            if (idStr == null) break;

            Integer driverId = Integer.valueOf(idStr);
            String hashKey = "driver:loc:" + driverId;

            Map<Object, Object> m = redis.opsForHash().entries(hashKey);
            if (m == null || m.isEmpty()) continue;

            double lat = Double.parseDouble((String) m.get("lat"));
            double lng = Double.parseDouble((String) m.get("lng"));

            driverLocationRepo.upsertLocation(driverId, lat, lng);
        }
    }
}

