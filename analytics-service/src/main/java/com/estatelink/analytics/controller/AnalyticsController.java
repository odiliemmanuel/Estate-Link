package com.estatelink.analytics.controller;

import com.estatelink.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> overview() {
        return ResponseEntity.ok(analyticsService.overview());
    }

    @GetMapping("/inspections")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> inspections(
            @RequestParam(defaultValue = "WEEK") String period) {
        Range range = Range.forPeriod(period);
        return ResponseEntity.ok(analyticsService.series("inspections", range.from(), range.to()));
    }

    @GetMapping("/offers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> offers(
            @RequestParam(defaultValue = "WEEK") String period) {
        Range range = Range.forPeriod(period);
        return ResponseEntity.ok(analyticsService.series("offers", range.from(), range.to()));
    }

    @GetMapping("/offers/accepted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> offersAccepted(
            @RequestParam(defaultValue = "WEEK") String period) {
        Range range = Range.forPeriod(period);
        return ResponseEntity.ok(analyticsService.series("offers.accepted", range.from(), range.to()));
    }

    @GetMapping("/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> agentPerformance() {
        return ResponseEntity.ok(analyticsService.agentPerformance());
    }

    private record Range(LocalDate from, LocalDate to) {
        static Range forPeriod(String period) {
            LocalDate to = LocalDate.now();
            LocalDate from = "MONTH".equalsIgnoreCase(period)
                    ? to.minusDays(30)
                    : to.minusDays(7);
            return new Range(from, to);
        }
    }
}
