package com.example.talkingCanvas.controller;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actuator")
public class ActuatorController {

    private final HealthEndpoint healthEndpoint;

    public ActuatorController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        HealthComponent health = healthEndpoint.health();
        return ResponseEntity.ok(Map.of(
            "status", health.getStatus().getCode()
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
            "app", "TalkingCanvas",
            "version", "1.0.0",
            "description", "TalkingCanvas Application Health and Info Endpoint",
            "endpoints", List.of(
                "/api/actuator/health",
                "/api/actuator/info"
            )
        ));
    }
}
