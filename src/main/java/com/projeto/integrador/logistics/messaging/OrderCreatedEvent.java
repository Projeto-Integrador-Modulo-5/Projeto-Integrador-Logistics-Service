package com.projeto.integrador.logistics.messaging;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    UUID userId,
    List<OrderItemEvent> items,
    UUID addressId,
    LocalDateTime createdAt
) {
    public record OrderItemEvent(UUID productId, String size, int quantity) {}
}
