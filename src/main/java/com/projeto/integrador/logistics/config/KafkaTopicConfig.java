package com.projeto.integrador.logistics.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara os tópicos Kafka com 3 partições e fator de replicação 3.
 */
@Configuration
public class KafkaTopicConfig {

    private static final int PARTITIONS    = 3;
    private static final short REPLICATION = 3;

    /** Pedido criado — backend → logistics */
    @Bean
    public NewTopic topicOrderCreated() {
        return TopicBuilder.name("order.created")
                .partitions(PARTITIONS)
                .replicas(REPLICATION)
                .build();
    }

    /** Atualização de status — logistics → backend + notification */
    @Bean
    public NewTopic topicOrderStatusUpdated() {
        return TopicBuilder.name("order.status.updated")
                .partitions(PARTITIONS)
                .replicas(REPLICATION)
                .build();
    }

    /** Envio de notificação — backend → notification */
    @Bean
    public NewTopic topicNotificationSend() {
        return TopicBuilder.name("notification.send")
                .partitions(PARTITIONS)
                .replicas(REPLICATION)
                .build();
    }
}
