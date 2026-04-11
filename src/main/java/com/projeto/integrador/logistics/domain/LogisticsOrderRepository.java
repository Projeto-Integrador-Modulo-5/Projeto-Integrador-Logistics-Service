package com.projeto.integrador.logistics.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface LogisticsOrderRepository extends JpaRepository<LogisticsOrder, UUID> {
    Optional<LogisticsOrder> findByOrderId(UUID orderId);
    boolean existsByOrderId(UUID orderId);
}
