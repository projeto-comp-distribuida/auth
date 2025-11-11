package com.distrischool.auth.controller;

import com.distrischool.auth.grpc.AuthGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para testar a funcionalidade gRPC.
 * Fornece endpoints para testar a comunicação gRPC entre serviços.
 */
@RestController
@RequestMapping("/api/grpc-test")
@RequiredArgsConstructor
@Slf4j
public class GrpcTestController {

    private final AuthGrpcClient authGrpcClient;

    /**
     * Testa a validação de token via gRPC
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String serviceName = request.getOrDefault("serviceName", "test-service");
        
        log.info("Testando validação de token via gRPC");
        
        AuthGrpcClient.TokenValidationResult result = authGrpcClient.validateToken(token, serviceName);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", result.isValid());
        response.put("userId", result.getUserId());
        response.put("email", result.getEmail());
        response.put("username", result.getUsername());
        response.put("roles", result.getRoles());
        response.put("expiresAt", result.getExpiresAt());
        response.put("errorMessage", result.getErrorMessage());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Testa a consulta de usuário por ID via gRPC
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
        log.info("Testando consulta de usuário via gRPC: {}", userId);
        
        AuthGrpcClient.UserInfo userInfo = authGrpcClient.getUserById(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("found", userInfo.isFound());
        response.put("userId", userInfo.getUserId());
        response.put("email", userInfo.getEmail());
        response.put("username", userInfo.getUsername());
        response.put("firstName", userInfo.getFirstName());
        response.put("lastName", userInfo.getLastName());
        response.put("active", userInfo.isActive());
        response.put("roles", userInfo.getRoles());
        response.put("createdAt", userInfo.getCreatedAt());
        response.put("updatedAt", userInfo.getUpdatedAt());
        response.put("errorMessage", userInfo.getErrorMessage());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Testa a verificação de role via gRPC
     */
    @GetMapping("/user/{userId}/has-role/{role}")
    public ResponseEntity<Map<String, Object>> hasRole(@PathVariable String userId, @PathVariable String role) {
        log.info("Testando verificação de role via gRPC - Usuário: {}, Role: {}", userId, role);
        
        boolean hasRole = authGrpcClient.hasRole(userId, role);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("role", role);
        response.put("hasRole", hasRole);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Testa a consulta de roles do usuário via gRPC
     */
    @GetMapping("/user/{userId}/roles")
    public ResponseEntity<Map<String, Object>> getUserRoles(@PathVariable String userId) {
        log.info("Testando consulta de roles via gRPC: {}", userId);
        
        var roles = authGrpcClient.getUserRoles(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("roles", roles);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Testa o health check via gRPC
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Testando health check via gRPC");
        
        boolean healthy = authGrpcClient.healthCheck("test-service");
        
        Map<String, Object> response = new HashMap<>();
        response.put("healthy", healthy);
        response.put("service", "auth-service");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}