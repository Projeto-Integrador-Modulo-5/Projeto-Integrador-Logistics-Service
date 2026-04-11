package com.projeto.integrador.logistics.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projeto.integrador.logistics.service.LogisticsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final LogisticsProcessor logisticsProcessor;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(LogisticsProcessor logisticsProcessor) {
        this.logisticsProcessor = logisticsProcessor;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "order.created", groupId = "logistics-group")
    public void consume(String message) {
        try {
            log.info("Recebido order.created: {}", message);
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            logisticsProcessor.processOrder(event);
        } catch (Exception e) {
            log.error("Erro ao processar order.created: {}", e.getMessage(), e);
        }
    }
}
