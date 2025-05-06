package com.name.match.health;

import com.name.match.service.NameMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthService implements HealthIndicator {

    private final NameMatchService nameMatchService;

    @Autowired
    public HealthService(NameMatchService nameMatchService) {
        this.nameMatchService = nameMatchService;
    }

    @Override
    public Health health() {
        try {
            // Test the service by calling mainFunction with null values
            nameMatchService.mainFunction(null, null);
            return Health.up().build();
        } catch (Exception e) {
            return Health.down().withDetail("error", "Service is down!!!").build();
        }
    }
} 