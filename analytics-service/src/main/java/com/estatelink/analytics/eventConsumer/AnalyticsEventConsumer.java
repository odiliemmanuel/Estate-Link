package com.estatelink.analytics.eventConsumer;

import com.estatelink.analytics.service.AnalyticsService;
import com.estatelink.common.event.InspectionRequestedEvent;
import com.estatelink.common.event.ListingApprovedEvent;
import com.estatelink.common.event.OfferAcceptedEvent;
import com.estatelink.common.event.OfferRejectedEvent;
import com.estatelink.common.event.OfferSentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyticsEventConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "listing.approved", groupId = "analytics-group",
            containerFactory = "listingApprovedListenerFactory")
    public void onListingApproved(ListingApprovedEvent event) {
        analyticsService.onListingApproved(event);
    }

    @KafkaListener(topics = "inspection.requested", groupId = "analytics-group",
            containerFactory = "inspectionRequestedListenerFactory")
    public void onInspectionRequested(InspectionRequestedEvent event) {
        analyticsService.onInspectionRequested(event);
    }

    @KafkaListener(topics = "offer.sent", groupId = "analytics-group",
            containerFactory = "offerSentListenerFactory")
    public void onOfferSent(OfferSentEvent event) {
        analyticsService.onOfferSent(event);
    }

    @KafkaListener(topics = "offer.accepted", groupId = "analytics-group",
            containerFactory = "offerAcceptedListenerFactory")
    public void onOfferAccepted(OfferAcceptedEvent event) {
        analyticsService.onOfferAccepted(event);
    }

    @KafkaListener(topics = "offer.rejected", groupId = "analytics-group",
            containerFactory = "offerRejectedListenerFactory")
    public void onOfferRejected(OfferRejectedEvent event) {
        analyticsService.onOfferRejected(event);
    }
}
