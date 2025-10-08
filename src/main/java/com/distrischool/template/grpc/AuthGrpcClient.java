package com.distrischool.template.grpc;

import com.distrischool.template.grpc.AuthServiceProto.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Cliente gRPC para comunicação com o serviço de autenticação.
 * Permite que outros microsserviços validem JWT tokens e consultem informações de usuários.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthGrpcClient {

    @GrpcClient("auth-service")
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

    /**
     * Valida um token JWT
     * 
     * @param token O token JWT a ser validado
     * @param serviceName Nome do serviço que está fazendo a validação
     * @return TokenValidationResult com informações do token
     */
    public TokenValidationResult validateToken(String token, String serviceName) {
        try {
            log.debug("Validando token JWT via gRPC para serviço: {}", serviceName);
            
            ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
                .setToken(token)
                .setServiceName(serviceName)
                .build();

            ValidateTokenResponse response = authServiceStub.validateToken(request);
            
            return TokenValidationResult.builder()
                .valid(response.getValid())
                .userId(response.getUserId())
                .email(response.getEmail())
                .username(response.getUsername())
                .roles(response.getRolesList())
                .expiresAt(response.getExpiresAt())
                .errorMessage(response.getErrorMessage())
                .build();
                
        } catch (StatusRuntimeException e) {
            log.error("Erro gRPC ao validar token: {}", e.getMessage(), e);
            return TokenValidationResult.builder()
                .valid(false)
                .errorMessage("Erro de comunicação com serviço de autenticação: " + e.getMessage())
                .build();
        } catch (Exception e) {
            log.error("Erro inesperado ao validar token via gRPC", e);
            return TokenValidationResult.builder()
                .valid(false)
                .errorMessage("Erro interno: " + e.getMessage())
                .build();
        }
    }

    /**
     * Obtém informações de um usuário pelo ID
     * 
     * @param userId ID do usuário
     * @return UserInfo com informações do usuário
     */
    public UserInfo getUserById(String userId) {
        try {
            log.debug("Consultando usuário por ID via gRPC: {}", userId);
            
            GetUserByIdRequest request = GetUserByIdRequest.newBuilder()
                .setUserId(userId)
                .build();

            GetUserByIdResponse response = authServiceStub.getUserById(request);
            
            if (response.getFound()) {
                return UserInfo.builder()
                    .found(true)
                    .userId(response.getUserId())
                    .email(response.getEmail())
                    .username(response.getUsername())
                    .firstName(response.getFirstName())
                    .lastName(response.getLastName())
                    .active(response.getActive())
                    .roles(response.getRolesList())
                    .createdAt(response.getCreatedAt())
                    .updatedAt(response.getUpdatedAt())
                    .build();
            } else {
                return UserInfo.builder()
                    .found(false)
                    .errorMessage(response.getErrorMessage())
                    .build();
            }
                
        } catch (StatusRuntimeException e) {
            log.error("Erro gRPC ao consultar usuário por ID: {}", e.getMessage(), e);
            return UserInfo.builder()
                .found(false)
                .errorMessage("Erro de comunicação com serviço de autenticação: " + e.getMessage())
                .build();
        } catch (Exception e) {
            log.error("Erro inesperado ao consultar usuário via gRPC", e);
            return UserInfo.builder()
                .found(false)
                .errorMessage("Erro interno: " + e.getMessage())
                .build();
        }
    }

    /**
     * Verifica se um usuário possui uma role específica
     * 
     * @param userId ID do usuário
     * @param role Nome da role
     * @return true se o usuário possui a role
     */
    public boolean hasRole(String userId, String role) {
        try {
            log.debug("Verificando role via gRPC - Usuário: {}, Role: {}", userId, role);
            
            HasRoleRequest request = HasRoleRequest.newBuilder()
                .setUserId(userId)
                .setRole(role)
                .build();

            HasRoleResponse response = authServiceStub.hasRole(request);
            
            return response.getHasRole();
                
        } catch (StatusRuntimeException e) {
            log.error("Erro gRPC ao verificar role: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Erro inesperado ao verificar role via gRPC", e);
            return false;
        }
    }

    /**
     * Obtém todas as roles de um usuário
     * 
     * @param userId ID do usuário
     * @return Lista de roles do usuário
     */
    public List<String> getUserRoles(String userId) {
        try {
            log.debug("Consultando roles do usuário via gRPC: {}", userId);
            
            GetUserRolesRequest request = GetUserRolesRequest.newBuilder()
                .setUserId(userId)
                .build();

            GetUserRolesResponse response = authServiceStub.getUserRoles(request);
            
            return response.getRolesList();
                
        } catch (StatusRuntimeException e) {
            log.error("Erro gRPC ao consultar roles: {}", e.getMessage(), e);
            return List.of();
        } catch (Exception e) {
            log.error("Erro inesperado ao consultar roles via gRPC", e);
            return List.of();
        }
    }

    /**
     * Verifica a saúde do serviço de autenticação
     * 
     * @param serviceName Nome do serviço que está fazendo o health check
     * @return true se o serviço está saudável
     */
    public boolean healthCheck(String serviceName) {
        try {
            log.debug("Health check via gRPC para serviço: {}", serviceName);
            
            HealthCheckRequest request = HealthCheckRequest.newBuilder()
                .setServiceName(serviceName)
                .build();

            HealthCheckResponse response = authServiceStub.healthCheck(request);
            
            return response.getHealthy();
                
        } catch (StatusRuntimeException e) {
            log.error("Erro gRPC no health check: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Erro inesperado no health check via gRPC", e);
            return false;
        }
    }

    /**
     * Resultado da validação de token
     */
    @lombok.Builder
    @lombok.Data
    public static class TokenValidationResult {
        private boolean valid;
        private String userId;
        private String email;
        private String username;
        private List<String> roles;
        private long expiresAt;
        private String errorMessage;
    }

    /**
     * Informações do usuário
     */
    @lombok.Builder
    @lombok.Data
    public static class UserInfo {
        private boolean found;
        private String userId;
        private String email;
        private String username;
        private String firstName;
        private String lastName;
        private boolean active;
        private List<String> roles;
        private long createdAt;
        private long updatedAt;
        private String errorMessage;
    }
}