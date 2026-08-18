package com.estatelink.offer.eventProducer;

import com.estatelink.common.event.OfferAcceptedEvent;
import com.estatelink.common.event.OfferRejectedEvent;
import com.estatelink.common.event.OfferSentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OfferEventProducer {

    private static final String TOPIC_OFFER_SENT = "offer.sent";
    private static final String TOPIC_OFFER_ACCEPTED = "offer.accepted";
    private static final String TOPIC_OFFER_REJECTED = "offer.rejected";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OfferEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOfferSent(OfferSentEvent event) {
        kafkaTemplate.send(TOPIC_OFFER_SENT, event.getAgentId().toString(), event)
                .whenComplete((result, ex) -> logSendResult("offer.sent", event.getOfferId(), ex));
    }

    public void publishOfferAccepted(OfferAcceptedEvent event) {
        kafkaTemplate.send(TOPIC_OFFER_ACCEPTED, event.getAgentId().toString(), event)
                .whenComplete((result, ex) -> logSendResult("offer.accepted", event.getOfferId(), ex));
    }

    public void publishOfferRejected(OfferRejectedEvent event) {
        kafkaTemplate.send(TOPIC_OFFER_REJECTED, event.getAgentId().toString(), event)
                .whenComplete((result, ex) -> logSendResult("offer.rejected", event.getOfferId(), ex));
    }

    private void logSendResult(String topic, Object offerId, Throwable ex) {
        if (ex != null) {
            log.error("Failed to publish {} for offer {}", topic, offerId, ex);
        } else {
            log.info("Published {} for offer {}", topic, offerId);
        }
    }
}
