package com.estatelink.analytics.repository;

import com.estatelink.analytics.domain.AgentMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AgentMetricRepository extends JpaRepository<AgentMetric, UUID> {
}
