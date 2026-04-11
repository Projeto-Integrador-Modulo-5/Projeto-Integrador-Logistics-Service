package com.projeto.integrador.logistics.messaging;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderStatusUpdatedEvent(
    UUID orderId,
    UUID userId,
    String newStatus,
    String trackingCode,
    LocalDateTime updatedAt
) {}
