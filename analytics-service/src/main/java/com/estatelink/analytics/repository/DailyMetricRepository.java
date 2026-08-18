package com.estatelink.analytics.repository;

import com.estatelink.analytics.domain.DailyMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyMetricRepository extends JpaRepository<DailyMetric, UUID> {

    Optional<DailyMetric> findByDateAndKey(LocalDate date, String key);
    List<DailyMetric> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
}
