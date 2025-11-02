package com.distrischool.template.controller;

import com.distrischool.template.dto.ApiResponse;
import com.distrischool.template.dto.auth.AuthResponse;
import com.distrischool.template.dto.auth.LoginRequest;
import com.distrischool.template.dto.auth.RegisterRequest;
import com.distrischool.template.service.AuthService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller de autenticação integrado com Auth0.
 * Gerencia endpoints de registro e outras operações de autenticação.
 * 
 * O backend interage com Auth0 Management API para criar usuários.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint de login
     * 
     * @param request dados de login (email e senha)
     * @return token JWT e informações do usuário
     */
    @PostMapping("/login")
    @Timed(value = "auth.login", description = "Time taken to login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - Email: {}", request.getEmail());
        
        AuthResponse authResponse = authService.login(request);
        
        return ResponseEntity.ok(
            ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Login realizado com sucesso")
                .data(authResponse)
                .build()
        );
    }

    /**
     * Endpoint de registro de novo usuário
     * 
     * @param request dados de registro
     * @return informações do usuário criado
     */
    @PostMapping("/register")
    @Timed(value = "auth.register", description = "Time taken to register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register - Email: {}", request.getEmail());
        
        AuthResponse authResponse = authService.register(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Registro realizado com sucesso. Verifique seu email para ativar a conta.")
                .data(authResponse)
                .build()
            );
    }

    /**
     * Endpoint interno para criar usuário (chamado por outros serviços)
     * Não requer senha, gera uma senha temporária
     * 
     * @param request dados do usuário a ser criado
     * @return informações do usuário criado
     */
    @PostMapping("/internal/users")
    @Timed(value = "auth.internal.createUser", description = "Time taken to create user internally")
    public ResponseEntity<ApiResponse<AuthResponse>> createUserInternal(@RequestBody com.distrischool.template.dto.auth.CreateUserInternalRequest request) {
        log.info("POST /api/v1/auth/internal/users - Email: {}", request.getEmail());
        
        AuthResponse authResponse = authService.createUserInternal(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<AuthResponse>builder()
                .success(true)
                .message("Usuário criado com sucesso")
                .data(authResponse)
                .build()
            );
    }
}

