package com.projeto.integrador.logistics.service;

import com.projeto.integrador.logistics.domain.LogisticsOrder;
import com.projeto.integrador.logistics.domain.LogisticsOrderRepository;
import com.projeto.integrador.logistics.domain.LogisticsStatus;
import com.projeto.integrador.logistics.messaging.OrderCreatedEvent;
import com.projeto.integrador.logistics.messaging.OrderStatusProducer;
import com.projeto.integrador.logistics.messaging.OrderStatusUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class LogisticsProcessor {

    private static final Logger log = LoggerFactory.getLogger(LogisticsProcessor.class);
    private static final Random RANDOM = new Random();

    private final LogisticsOrderRepository repository;
    private final OrderStatusProducer producer;

    @Value("${app.logistics.shipping-delay-ms:8000}")
    private long shippingDelayMs;

    @Value("${app.logistics.delivery-delay-ms:15000}")
    private long deliveryDelayMs;

    public LogisticsProcessor(LogisticsOrderRepository repository, OrderStatusProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    public void processOrder(OrderCreatedEvent event) {
        // Idempotencia: ignora se ja processou
        if (repository.existsByOrderId(event.orderId())) {
            log.warn("Pedido {} ja foi processado. Ignorando.", event.orderId());
            return;
        }

        // Salva o registro logistico
        LogisticsOrder logisticsOrder = new LogisticsOrder();
        logisticsOrder.setOrderId(event.orderId());
        logisticsOrder.setUserId(event.userId());
        logisticsOrder.setStatus(LogisticsStatus.PROCESSING);
        logisticsOrder = repository.save(logisticsOrder);

        final LogisticsOrder finalOrder = logisticsOrder;
        final String trackingCode = generateTrackingCode();
        final String shortId = event.orderId().toString().substring(0, 8).toUpperCase();

        log.info("");
        log.info("+---------------------------------------------------+");
        log.info("|  KAFKA >> [order.created] consumido               |");
        log.info("|  Pedido   : #{}                          |", shortId);
        log.info("|  Tracking : {}                         |", trackingCode);
        log.info("|  Status   : PROCESSING                            |");
        log.info("|  Aguardando {}ms para simular envio...        |", shippingDelayMs);
        log.info("+---------------------------------------------------+");

        // Usa Virtual Thread para nao bloquear o consumer do Kafka
        Thread.ofVirtual().name("logistics-" + event.orderId()).start(() -> {
            try {
                // Simula processamento -> SHIPPED
                Thread.sleep(shippingDelayMs);
                finalOrder.setStatus(LogisticsStatus.SHIPPED);
                finalOrder.setTrackingCode(trackingCode);
                finalOrder.setShippedAt(LocalDateTime.now());
                repository.save(finalOrder);

                producer.publishStatusUpdate(new OrderStatusUpdatedEvent(
                    event.orderId(), event.userId(), "SHIPPED", trackingCode, LocalDateTime.now()
                ));

                log.info("");
                log.info("+---------------------------------------------------+");
                log.info("|  KAFKA << [order.status.updated] publicado        |");
                log.info("|  Pedido   : #{}                          |", shortId);
                log.info("|  Status   : SHIPPED [em transito]                 |");
                log.info("|  Tracking : {}                         |", trackingCode);
                log.info("|  Aguardando {}ms para simular entrega...     |", deliveryDelayMs);
                log.info("+---------------------------------------------------+");

                // Simula entrega -> DELIVERED
                Thread.sleep(deliveryDelayMs);
                finalOrder.setStatus(LogisticsStatus.DELIVERED);
                finalOrder.setDeliveredAt(LocalDateTime.now());
                repository.save(finalOrder);

                producer.publishStatusUpdate(new OrderStatusUpdatedEvent(
                    event.orderId(), event.userId(), "DELIVERED", trackingCode, LocalDateTime.now()
                ));

                log.info("");
                log.info("+---------------------------------------------------+");
                log.info("|  KAFKA << [order.status.updated] publicado        |");
                log.info("|  Pedido   : #{}                          |", shortId);
                log.info("|  Status   : DELIVERED [entregue]                  |");
                log.info("|  Fluxo completo para este pedido.                 |");
                log.info("+---------------------------------------------------+");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrompida para pedido {}", event.orderId());
            }
        });
    }

    private String generateTrackingCode() {
        int number = 100_000_000 + RANDOM.nextInt(900_000_000);
        return "BR" + number + "PT";
    }
}
