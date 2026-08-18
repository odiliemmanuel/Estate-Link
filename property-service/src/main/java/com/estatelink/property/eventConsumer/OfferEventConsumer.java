package com.estatelink.property.eventConsumer;

import com.estatelink.common.event.OfferAcceptedEvent;
import com.estatelink.common.event.OfferSentEvent;
import com.estatelink.property.service.ListingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Keeps listing status in sync with the offer lifecycle:
 * offer.sent -> UNDER_OFFER, offer.accepted -> CLOSED.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfferEventConsumer {

    private final ListingService listingService;

    @KafkaListener(topics = "offer.sent", groupId = "property-offer-group",
            containerFactory = "offerSentListenerFactory")
    public void onOfferSent(OfferSentEvent event) {
        listingService.markUnderOffer(event.getListingId());
        log.info("Listing {} moved to UNDER_OFFER by offer {}", event.getListingId(), event.getOfferId());
    }

    @KafkaListener(topics = "offer.accepted", groupId = "property-offer-group",
            containerFactory = "offerAcceptedListenerFactory")
    public void onOfferAccepted(OfferAcceptedEvent event) {
        listingService.closeListing(event.getListingId());
        log.info("Listing {} closed by accepted offer {}", event.getListingId(), event.getOfferId());
    }
}
