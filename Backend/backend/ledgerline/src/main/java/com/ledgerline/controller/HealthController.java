package com.ledgerline.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "application", "LedgerLine",
                "status", "UP",
                "message", "LedgerLine backend is running"
        );
    }
}
