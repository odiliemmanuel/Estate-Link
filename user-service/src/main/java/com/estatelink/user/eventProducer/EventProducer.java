package com.estatelink.user.eventProducer;

import com.estatelink.user.event.UserRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {


    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    public EventProducer(KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    public void send(UserRegisteredEvent event) {

        Message<UserRegisteredEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.KEY, event.getVerificationToken())
                .setHeader(KafkaHeaders.TOPIC, "user.registered")
                .build();
        try{
            kafkaTemplate.send(message);
        }catch (Exception e){
            System.err.println("Failed to publish user-registered event for " + event.getEmail() + ": " + e.getMessage());
        }

    }

}
