package com.distrischool.auth.controller;

import com.distrischool.auth.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para endpoints relacionados ao Auth0.
 * Fornece informações sobre o status da integração Auth0.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class Auth0Controller {

    /**
     * Health check para o serviço de autenticação
     * 
     * @return status do serviço
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Auth0Status>> health() {
        log.info("GET /api/v1/auth/health - Health check do serviço de autenticação");
        
        Auth0Status status = Auth0Status.builder()
            .service("DistriSchool Auth Service")
            .status("UP")
            .authProvider("Auth0")
            .message("Auth0 integration is active. All authentication is managed by Auth0.")
            .build();
        
        return ResponseEntity.ok(
            ApiResponse.<Auth0Status>builder()
                .success(true)
                .message("Serviço de autenticação funcionando corretamente")
                .data(status)
                .build()
        );
    }

    /**
     * Informações sobre a configuração Auth0
     * 
     * @return informações da configuração Auth0
     */
    @GetMapping("/auth0/info")
    public ResponseEntity<ApiResponse<Auth0Info>> getAuth0Info() {
        log.info("GET /api/v1/auth/auth0/info - Informações da configuração Auth0");
        
        Auth0Info info = Auth0Info.builder()
            .authProvider("Auth0")
            .features(new String[]{
                "OAuth2/OIDC Authentication",
                "JWT Token Management", 
                "Email Verification",
                "Password Reset",
                "Multi-Factor Authentication",
                "Social Login (Google, Facebook, etc.)",
                "Anomaly Detection",
                "Brute Force Protection"
            })
            .message("All authentication features are managed by Auth0")
            .build();
        
        return ResponseEntity.ok(
            ApiResponse.<Auth0Info>builder()
                .success(true)
                .message("Informações da configuração Auth0")
                .data(info)
                .build()
        );
    }

    /**
     * DTO para status do Auth0
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Auth0Status {
        private String service;
        private String status;
        private String authProvider;
        private String message;
    }

    /**
     * DTO para informações do Auth0
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Auth0Info {
        private String authProvider;
        private String[] features;
        private String message;
    }
}

