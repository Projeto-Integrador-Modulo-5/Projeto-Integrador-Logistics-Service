package com.projeto.integrador.logistics.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusProducer.class);
    private static final String TOPIC = "order.status.updated";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderStatusProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void publishStatusUpdate(OrderStatusUpdatedEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            log.info("Publicando {} -> orderId={}", TOPIC, event.orderId());
            kafkaTemplate.send(TOPIC, event.orderId().toString(), json);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar OrderStatusUpdatedEvent", e);
            throw new RuntimeException("Erro ao publicar status update", e);
        }
    }
}
