package com.distrischool.template.controller;

import com.distrischool.template.dto.ApiResponse;
import com.distrischool.template.dto.auth.AuthResponse;
import com.distrischool.template.dto.auth.LoginRequest;
import com.distrischool.template.dto.auth.RegisterRequest;
import com.distrischool.template.dto.auth.ForgotPasswordRequest;
import com.distrischool.template.dto.auth.ResetPasswordRequest;
import com.distrischool.template.dto.auth.VerifyEmailRequest;
import com.distrischool.template.security.UserPrincipal;
import com.distrischool.template.service.AuthService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    @Timed(
        value = "auth.login",
        description = "Time taken to login",
        percentiles = {0.5, 0.9, 0.95, 0.99},
        histogram = true
    )
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
    @Timed(
        value = "auth.register",
        description = "Time taken to register",
        percentiles = {0.5, 0.9, 0.95, 0.99},
        histogram = true
    )
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
    @Timed(
        value = "auth.internal.createUser",
        description = "Time taken to create user internally",
        percentiles = {0.5, 0.9, 0.95, 0.99},
        histogram = true
    )
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

    /**
     * Endpoint para obter informações do usuário autenticado atual
     * 
     * @return informações do usuário autenticado
     */
    @GetMapping("/me")
    @Timed(
        value = "auth.me",
        description = "Time taken to get current user info",
        percentiles = {0.5, 0.9, 0.95, 0.99},
        histogram = true
    )
    public ResponseEntity<ApiResponse<AuthResponse.UserResponse>> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<AuthResponse.UserResponse>builder()
                    .success(false)
                    .message("Usuário não autenticado")
                    .build()
                );
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        if (userPrincipal.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<AuthResponse.UserResponse>builder()
                    .success(false)
                    .message("Usuário não encontrado no sistema")
                    .build()
                );
        }
        
        log.info("GET /api/v1/auth/me - User ID: {}", userPrincipal.getId());
        
        AuthResponse.UserResponse userResponse = authService.getCurrentUser(userPrincipal.getId());
        
        return ResponseEntity.ok(
            ApiResponse.<AuthResponse.UserResponse>builder()
                .success(true)
                .message("Informações do usuário obtidas com sucesso")
                .data(userResponse)
                .build()
        );
    }

    /**
     * Endpoint para solicitar reset de senha via Auth0
     */
    @PostMapping("/forgot-password")
    @Timed(
        value = "auth.forgotPassword",
        description = "Time taken to request password reset",
        percentiles = {0.5, 0.9, 0.95, 0.99},
        histogram = true
    )
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("POST /api/v1/auth/forgot-password - Email: {}", request.getEmail());

        authService.initiatePasswordReset(request.getEmail());

        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Se o email estiver cadastrado, enviaremos instruções para redefinir a senha.")
                .build()
        );
    }

    /**
     * Endpoint para resetar senha usando token
     */
    @PostMapping("/reset-password")
    @Timed(
        value = "auth.resetPassword",
        description = "Time taken to reset password",
        percentiles = {0.5, 0.9, 0.95, 0.99},
        histogram = true
    )
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("POST /api/v1/auth/reset-password");

        authService.resetPassword(request.getToken(), request.getNewPassword(), request.getConfirmPassword());

        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Senha redefinida com sucesso.")
                .build()
        );
    }

    /**
     * Endpoint para verificar email usando token (POST)
     */
    @PostMapping("/verify-email")
    @Timed(
        value = "auth.verifyEmail",
        description = "Time taken to verify email",
        percentiles = {0.5, 0.9, 0.95, 0.99},
        histogram = true
    )
    public ResponseEntity<ApiResponse<Void>> verifyEmailPost(@Valid @RequestBody VerifyEmailRequest request) {
        log.info("POST /api/v1/auth/verify-email");

        authService.verifyEmail(request.getToken());

        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Email verificado com sucesso.")
                .build()
        );
    }

    /**
     * Endpoint para verificar email usando token (GET - para links em emails)
     */
    @GetMapping("/verify-email/{token}")
    @Timed(
        value = "auth.verifyEmail",
        description = "Time taken to verify email",
        percentiles = {0.5, 0.9, 0.95, 0.99},
        histogram = true
    )
    public ResponseEntity<ApiResponse<Void>> verifyEmailGet(@PathVariable String token) {
        log.info("GET /api/v1/auth/verify-email/{}", token);

        authService.verifyEmail(token);

        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Email verificado com sucesso.")
                .build()
        );
    }
}

