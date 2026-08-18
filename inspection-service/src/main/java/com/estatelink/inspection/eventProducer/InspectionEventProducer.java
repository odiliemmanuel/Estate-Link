package com.estatelink.inspection.eventProducer;

import com.estatelink.inspection.event.InspectionRequestedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InspectionEventProducer {

    private static final String TOPIC_INSPECTION_REQUESTED = "inspection.requested";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public InspectionEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishInspectionRequested(InspectionRequestedEvent event) {
        kafkaTemplate.send(TOPIC_INSPECTION_REQUESTED, event.getAgentId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish inspection.requested for request {}",
                                event.getRequestId(), ex);
                    } else {
                        log.info("Published inspection.requested for request {} -> topic {}, partition {}, offset {}",
                                event.getRequestId(), result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
