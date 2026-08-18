package com.estatelink.property.eventProducer;

import com.estatelink.property.event.ListingApprovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendListingApprovedEvent(ListingApprovedEvent event){

        kafkaTemplate.send("listing.approved", event.getListingId().toString(), event);
        log.info("Published listing.approved event for listingId={}", event.getListingId());
    }
}
