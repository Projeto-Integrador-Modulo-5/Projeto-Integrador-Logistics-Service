package com.projeto.integrador.logistics.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class HelloProducer {

    private static final Logger log = LoggerFactory.getLogger(HelloProducer.class);
    private static final String TOPIC = "hello.world";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public HelloProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String message) {
        log.info("Publicando no Kafka → tópico: {} | mensagem: {}", TOPIC, message);
        kafkaTemplate.send(TOPIC, message);
    }
}
