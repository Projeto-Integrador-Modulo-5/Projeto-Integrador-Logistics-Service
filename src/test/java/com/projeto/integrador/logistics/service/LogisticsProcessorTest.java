package com.projeto.integrador.logistics.service;

import com.projeto.integrador.logistics.domain.LogisticsOrder;
import com.projeto.integrador.logistics.domain.LogisticsOrderRepository;
import com.projeto.integrador.logistics.domain.LogisticsStatus;
import com.projeto.integrador.logistics.messaging.OrderCreatedEvent;
import com.projeto.integrador.logistics.messaging.OrderStatusProducer;
import com.projeto.integrador.logistics.messaging.OrderStatusUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogisticsProcessorTest {

    @Mock private LogisticsOrderRepository repository;
    @Mock private OrderStatusProducer producer;

    @InjectMocks private LogisticsProcessor logisticsProcessor;

    private UUID orderId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId  = UUID.randomUUID();
        // Delays mínimos para que os testes não sejam lentos
        ReflectionTestUtils.setField(logisticsProcessor, "shippingDelayMs", 50L);
        ReflectionTestUtils.setField(logisticsProcessor, "deliveryDelayMs", 50L);
    }

    // ── idempotência ──────────────────────────────────────────────────────────

    @Test
    void processOrder_shouldIgnoreWhenOrderAlreadyProcessed() {
        when(repository.existsByOrderId(orderId)).thenReturn(true);

        OrderCreatedEvent event = buildEvent(orderId, userId);
        logisticsProcessor.processOrder(event);

        verify(repository, never()).save(any());
        verify(producer, never()).publishStatusUpdate(any());
    }

    // ── novo pedido ───────────────────────────────────────────────────────────

    @Test
    void processOrder_shouldSaveLogisticsOrderWithProcessingStatus() throws InterruptedException {
        when(repository.existsByOrderId(orderId)).thenReturn(false);
        LogisticsOrder savedOrder = new LogisticsOrder();
        savedOrder.setOrderId(orderId);
        savedOrder.setUserId(userId);
        savedOrder.setStatus(LogisticsStatus.PROCESSING);
        when(repository.save(any(LogisticsOrder.class))).thenReturn(savedOrder);

        OrderCreatedEvent event = buildEvent(orderId, userId);
        logisticsProcessor.processOrder(event);

        ArgumentCaptor<LogisticsOrder> captor = ArgumentCaptor.forClass(LogisticsOrder.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(LogisticsStatus.PROCESSING);
        assertThat(captor.getValue().getOrderId()).isEqualTo(orderId);
    }

    @Test
    void processOrder_shouldPublishShippedAndDeliveredStatusUpdates() throws InterruptedException {
        when(repository.existsByOrderId(orderId)).thenReturn(false);
        LogisticsOrder savedOrder = new LogisticsOrder();
        savedOrder.setOrderId(orderId);
        savedOrder.setUserId(userId);
        savedOrder.setStatus(LogisticsStatus.PROCESSING);
        when(repository.save(any(LogisticsOrder.class))).thenReturn(savedOrder);

        OrderCreatedEvent event = buildEvent(orderId, userId);
        logisticsProcessor.processOrder(event);

        // Aguarda a virtual thread completar (delays de 50ms cada)
        TimeUnit.MILLISECONDS.sleep(500);

        ArgumentCaptor<OrderStatusUpdatedEvent> captor =
            ArgumentCaptor.forClass(OrderStatusUpdatedEvent.class);
        verify(producer, times(2)).publishStatusUpdate(captor.capture());

        List<OrderStatusUpdatedEvent> events = captor.getAllValues();
        assertThat(events.get(0).newStatus()).isEqualTo("SHIPPED");
        assertThat(events.get(1).newStatus()).isEqualTo("DELIVERED");
        assertThat(events.get(0).orderId()).isEqualTo(orderId);
    }

    // ── formato do tracking code ──────────────────────────────────────────────

    @Test
    void processOrder_shouldGenerateTrackingCodeInCorrectFormat() throws InterruptedException {
        when(repository.existsByOrderId(orderId)).thenReturn(false);
        LogisticsOrder savedOrder = new LogisticsOrder();
        savedOrder.setOrderId(orderId);
        savedOrder.setUserId(userId);
        savedOrder.setStatus(LogisticsStatus.PROCESSING);
        when(repository.save(any(LogisticsOrder.class))).thenReturn(savedOrder);

        OrderCreatedEvent event = buildEvent(orderId, userId);
        logisticsProcessor.processOrder(event);

        TimeUnit.MILLISECONDS.sleep(500);

        ArgumentCaptor<OrderStatusUpdatedEvent> captor =
            ArgumentCaptor.forClass(OrderStatusUpdatedEvent.class);
        verify(producer, atLeastOnce()).publishStatusUpdate(captor.capture());

        String trackingCode = captor.getValue().trackingCode();
        assertThat(trackingCode).matches("BR\\d{9}PT");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private OrderCreatedEvent buildEvent(UUID orderId, UUID userId) {
        return new OrderCreatedEvent(orderId, userId, List.of(), UUID.randomUUID(), LocalDateTime.now());
    }
}
