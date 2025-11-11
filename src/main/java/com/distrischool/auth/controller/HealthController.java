package com.distrischool.auth.controller;

import com.distrischool.auth.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller base para demonstrar a estrutura do DistriSchool.
 * Este controller serve como exemplo de como implementar endpoints REST
 * seguindo os padrões do sistema de gestão escolar.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealth() {
        log.info("GET /api/v1/health - Verificando saúde do serviço");
        
        Map<String, Object> healthInfo = Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "DistriSchool Microservice Template",
            "version", "1.0.0"
        );
        
        return ResponseEntity.ok(ApiResponse.success(healthInfo, "Serviço funcionando corretamente"));
    }

    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInfo() {
        log.info("GET /api/v1/health/info - Obtendo informações do serviço");
        
        Map<String, Object> serviceInfo = Map.of(
            "name", "DistriSchool Microservice Template",
            "description", "Template base para microsserviços do sistema de gestão escolar",
            "version", "1.0.0",
            "features", new String[]{
                "Spring Boot 3.2.0",
                "PostgreSQL com Flyway",
                "Redis para cache",
                "Apache Kafka para mensageria",
                "Spring Cloud OpenFeign",
                "Resilience4j Circuit Breaker",
                "Prometheus Metrics"
            }
        );
        
        return ResponseEntity.ok(ApiResponse.success(serviceInfo, "Informações do serviço"));
    }
}
