package com.projeto.integrador.logistics.controller;

import com.projeto.integrador.logistics.domain.LogisticsOrder;
import com.projeto.integrador.logistics.domain.LogisticsOrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/logistics/orders")
public class LogisticsController {

    private final LogisticsOrderRepository repository;

    public LogisticsController(LogisticsOrderRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<LogisticsOrder> getOrder(@PathVariable UUID orderId) {
        return repository.findByOrderId(orderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
