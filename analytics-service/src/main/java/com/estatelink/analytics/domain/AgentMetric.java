package com.estatelink.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Per-agent performance counters, updated from inspection and offer events.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "agent_metrics")
public class AgentMetric {

    @Id
    private UUID agentId;

    @Column(nullable = false)
    private long inspectionsReceived;

    @Column(nullable = false)
    private long inspectionsAccepted;

    @Column(nullable = false)
    private long offersSent;

    @Column(nullable = false)
    private long offersAccepted;

    private LocalDateTime lastActivityAt;
}
