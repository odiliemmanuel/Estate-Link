package com.estatelink.analytics.service;

import com.estatelink.analytics.domain.AgentMetric;
import com.estatelink.analytics.domain.DailyMetric;
import com.estatelink.analytics.domain.ListingAnalytic;
import com.estatelink.analytics.repository.AgentMetricRepository;
import com.estatelink.analytics.repository.DailyMetricRepository;
import com.estatelink.analytics.repository.ListingAnalyticRepository;
import com.estatelink.common.event.InspectionRequestedEvent;
import com.estatelink.common.event.ListingApprovedEvent;
import com.estatelink.common.event.OfferAcceptedEvent;
import com.estatelink.common.event.OfferRejectedEvent;
import com.estatelink.common.event.OfferSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Materialised aggregates maintained from Kafka domain events. Note: counts
 * are event-driven and may under-count data that existed before this service
 * first started consuming (at-least-once delivery may also over-count on
 * replays) — acceptable for a demo dashboard.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ListingAnalyticRepository listingRepository;
    private final DailyMetricRepository dailyMetricRepository;
    private final AgentMetricRepository agentMetricRepository;

    // ── Event handling ───────────────────────────────────────────────────

    @Transactional
    public void onListingApproved(ListingApprovedEvent event) {
        upsertListing(event.getListingId(), "ACTIVE", event.getApprovedAt());
        incrementDaily("listings", LocalDate.now());
        log.info("Analytics: listing {} approved", event.getListingId());
    }

    @Transactional
    public void onInspectionRequested(InspectionRequestedEvent event) {
        incrementDaily("inspections", LocalDate.now());
        AgentMetric metric = agentMetric(event.getAgentId());
        metric.setInspectionsReceived(metric.getInspectionsReceived() + 1);
        log.info("Analytics: inspection {} requested on listing {}", event.getRequestId(), event.getListingId());
    }

    @Transactional
    public void onInspectionAccepted(UUID agentId) {
        AgentMetric metric = agentMetric(agentId);
        metric.setInspectionsAccepted(metric.getInspectionsAccepted() + 1);
    }

    @Transactional
    public void onOfferSent(OfferSentEvent event) {
        listingRepository.findById(event.getListingId())
                .filter(l -> "ACTIVE".equals(l.getStatus()))
                .ifPresent(l -> {
                    l.setStatus("UNDER_OFFER");
                    l.setLastEventAt(LocalDateTime.now());
                    listingRepository.save(l);
                });
        incrementDaily("offers", LocalDate.now());
        AgentMetric metric = agentMetric(event.getAgentId());
        metric.setOffersSent(metric.getOffersSent() + 1);
        log.info("Analytics: offer {} sent on listing {}", event.getOfferId(), event.getListingId());
    }

    @Transactional
    public void onOfferAccepted(OfferAcceptedEvent event) {
        upsertListing(event.getListingId(), "CLOSED", null);
        incrementDaily("offers.accepted", LocalDate.now());
        AgentMetric metric = agentMetric(event.getAgentId());
        metric.setOffersAccepted(metric.getOffersAccepted() + 1);
        log.info("Analytics: offer {} accepted", event.getOfferId());
    }

    @Transactional
    public void onOfferRejected(OfferRejectedEvent event) {
        incrementDaily("offers.rejected", LocalDate.now());
        log.info("Analytics: offer {} rejected", event.getOfferId());
    }

    // ── Queries ──────────────────────────────────────────────────────────

    public Map<String, Object> overview() {
        List<ListingAnalytic> listings = listingRepository.findAll();
        Map<String, Long> byStatus = listings.stream()
                .collect(Collectors.groupingBy(ListingAnalytic::getStatus, Collectors.counting()));

        return Map.of(
                "listingsByStatus", byStatus,
                "listingsTotal", (long) listings.size(),
                "agentsActive", (long) agentMetricRepository.count(),
                "inspections", dayMetric("inspections") + dayMetric("inspections.accepted"),
                "offersSent", dayMetric("offers"),
                "offersAccepted", dayMetric("offers.accepted"),
                "offersRejected", dayMetric("offers.rejected")
        );
    }

    public List<Map<String, Object>> series(String key, LocalDate from, LocalDate to) {
        return dailyMetricRepository.findByDateBetweenOrderByDateAsc(from, to).stream()
                .filter(m -> key.equals(m.getKey()))
                .map(m -> Map.<String, Object>of(
                        "date", m.getDate().toString(),
                        "value", m.getValue()))
                .toList();
    }

    public List<AgentMetric> agentPerformance() {
        return agentMetricRepository.findAll();
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private void upsertListing(UUID listingId, String status, LocalDateTime approvedAt) {
        ListingAnalytic analytic = listingRepository.findById(listingId)
                .orElseGet(() -> ListingAnalytic.builder().listingId(listingId).build());
        analytic.setStatus(status);
        if (approvedAt != null && analytic.getApprovedAt() == null) {
            analytic.setApprovedAt(approvedAt);
        }
        analytic.setLastEventAt(LocalDateTime.now());
        listingRepository.save(analytic);
    }

    private AgentMetric agentMetric(UUID agentId) {
        return agentMetricRepository.findById(agentId)
                .orElseGet(() -> agentMetricRepository.save(
                        AgentMetric.builder().agentId(agentId).build()));
    }

    private long dayMetric(String key) {
        return dailyMetricRepository.findByDateAndKey(LocalDate.now(), key)
                .map(DailyMetric::getValue).orElse(0L);
    }

    private void incrementDaily(String key, LocalDate date) {
        DailyMetric metric = dailyMetricRepository.findByDateAndKey(date, key)
                .orElseGet(() -> DailyMetric.builder()
                        .date(date).key(key).value(0).build());
        metric.setValue(metric.getValue() + 1);
        metric.setLastEventAt(LocalDateTime.now());
        dailyMetricRepository.save(metric);
    }
}
