package com.estatelink.analytics.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A single increment for a metric key on a given date, e.g. ("2026-08-05",
 * "inspections", 3). Enables time-series dashboard queries.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "daily_metrics", uniqueConstraints =
        @UniqueConstraint(columnNames = {"metric_date", "metric_key"}))
public class DailyMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "metric_date", nullable = false)
    private LocalDate date;

    @Column(name = "metric_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private long value;

    private LocalDateTime lastEventAt;
}
