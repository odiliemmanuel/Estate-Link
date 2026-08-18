package com.estatelink.analytics.config;

import com.estatelink.common.event.InspectionRequestedEvent;
import com.estatelink.common.event.ListingApprovedEvent;
import com.estatelink.common.event.OfferAcceptedEvent;
import com.estatelink.common.event.OfferRejectedEvent;
import com.estatelink.common.event.OfferSentEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumer factories, one per event type. Producers disable JSON type headers,
 * so each listener must target its DTO explicitly.
 */
@Configuration
public class KafkaConfig {

    private static final String GROUP = "analytics-group";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, ListingApprovedEvent>>
    listingApprovedListenerFactory() {
        return factory(ListingApprovedEvent.class);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, InspectionRequestedEvent>>
    inspectionRequestedListenerFactory() {
        return factory(InspectionRequestedEvent.class);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, OfferSentEvent>>
    offerSentListenerFactory() {
        return factory(OfferSentEvent.class);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, OfferAcceptedEvent>>
    offerAcceptedListenerFactory() {
        return factory(OfferAcceptedEvent.class);
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, OfferRejectedEvent>>
    offerRejectedListenerFactory() {
        return factory(OfferRejectedEvent.class);
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> factory(Class<T> type) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        JsonDeserializer<T> valueDeserializer = new JsonDeserializer<>(type, false);
        valueDeserializer.addTrustedPackages("*");

        ConsumerFactory<String, T> consumerFactory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);

        ConcurrentKafkaListenerContainerFactory<String, T> containerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        containerFactory.setConsumerFactory(consumerFactory);
        return containerFactory;
    }
}
