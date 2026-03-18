package com.projeto.integrador.logistics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projeto.integrador.logistics.messaging.HelloProducer;

@RestController
@RequestMapping("/api/hello")
public class HelloController {

    private final HelloProducer helloProducer;

    public HelloController(HelloProducer helloProducer) {
        this.helloProducer = helloProducer;
    }

    @PostMapping
    public ResponseEntity<String> sendHello() {
        helloProducer.send("Hello World! Kafka está funcionando!");
        return ResponseEntity.ok("Mensagem publicada no Kafka!");
    }
}
