package com.projeto.integrador.logistics.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de integração — requer infraestrutura completa (PostgreSQL, Kafka).
 * Desabilitado na suite de testes unitários.
 */
@Disabled("Teste de integração: requer PostgreSQL e Kafka rodando")
@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
