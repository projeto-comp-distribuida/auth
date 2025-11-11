package com.distrischool.auth.grpc;

import com.distrischool.auth.entity.User;
import com.distrischool.auth.entity.UserRole;
import com.distrischool.auth.repository.UserRepository;
import com.distrischool.auth.service.JwtService;
import com.distrischool.auth.grpc.AuthServiceProto.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementação do serviço gRPC de autenticação.
 * Fornece validação de JWT e consultas de usuário para outros microsserviços.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        log.debug("Validação de token JWT solicitada pelo serviço: {}", request.getServiceName());
        
        try {
            // Valida o token JWT
            if (jwtService.isTokenValid(request.getToken())) {
                // Extrai informações do token
                String userId = jwtService.extractUserId(request.getToken());
                String email = jwtService.extractEmail(request.getToken());
                String username = jwtService.extractUsername(request.getToken());
                List<String> roles = jwtService.extractRoles(request.getToken());
                long expiresAt = jwtService.extractExpiration(request.getToken()).getTime();

                ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setValid(true)
                    .setUserId(userId)
                    .setEmail(email)
                    .setUsername(username)
                    .addAllRoles(roles)
                    .setExpiresAt(expiresAt)
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                
                log.debug("Token JWT válido para usuário: {}", email);
            } else {
                ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                    .setValid(false)
                    .setErrorMessage("Token inválido ou expirado")
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                
                log.debug("Token JWT inválido");
            }
        } catch (Exception e) {
            log.error("Erro ao validar token JWT", e);
            
            ValidateTokenResponse response = ValidateTokenResponse.newBuilder()
                .setValid(false)
                .setErrorMessage("Erro interno na validação do token: " + e.getMessage())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<GetUserByIdResponse> responseObserver) {
        log.debug("Consulta de usuário por ID solicitada: {}", request.getUserId());
        
        try {
            Optional<User> userOpt = userRepository.findById(Long.parseLong(request.getUserId()));
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                GetUserByIdResponse response = GetUserByIdResponse.newBuilder()
                    .setFound(true)
                    .setUserId(user.getId().toString())
                    .setEmail(user.getEmail())
                    .setUsername(user.getEmail()) // Usando email como username
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName())
                    .setActive(user.getActive())
                    .addAllRoles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()))
                    .setCreatedAt(user.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                    .setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toEpochSecond(ZoneOffset.UTC) : 0)
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                
                log.debug("Usuário encontrado: {}", user.getEmail());
            } else {
                GetUserByIdResponse response = GetUserByIdResponse.newBuilder()
                    .setFound(false)
                    .setErrorMessage("Usuário não encontrado")
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                
                log.debug("Usuário não encontrado: {}", request.getUserId());
            }
        } catch (NumberFormatException e) {
            log.error("ID de usuário inválido: {}", request.getUserId(), e);
            
            GetUserByIdResponse response = GetUserByIdResponse.newBuilder()
                .setFound(false)
                .setErrorMessage("ID de usuário inválido")
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Erro ao consultar usuário por ID", e);
            
            GetUserByIdResponse response = GetUserByIdResponse.newBuilder()
                .setFound(false)
                .setErrorMessage("Erro interno: " + e.getMessage())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void hasRole(HasRoleRequest request, StreamObserver<HasRoleResponse> responseObserver) {
        log.debug("Verificação de role solicitada - Usuário: {}, Role: {}", request.getUserId(), request.getRole());
        
        try {
            Optional<User> userOpt = userRepository.findById(Long.parseLong(request.getUserId()));
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean hasRole = user.getRoles().stream()
                    .anyMatch(role -> role.getName().name().equals(request.getRole()));
                
                HasRoleResponse response = HasRoleResponse.newBuilder()
                    .setHasRole(hasRole)
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                
                log.debug("Verificação de role - Usuário: {}, Role: {}, Resultado: {}", 
                    request.getUserId(), request.getRole(), hasRole);
            } else {
                HasRoleResponse response = HasRoleResponse.newBuilder()
                    .setHasRole(false)
                    .setErrorMessage("Usuário não encontrado")
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (NumberFormatException e) {
            log.error("ID de usuário inválido: {}", request.getUserId(), e);
            
            HasRoleResponse response = HasRoleResponse.newBuilder()
                .setHasRole(false)
                .setErrorMessage("ID de usuário inválido")
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Erro ao verificar role do usuário", e);
            
            HasRoleResponse response = HasRoleResponse.newBuilder()
                .setHasRole(false)
                .setErrorMessage("Erro interno: " + e.getMessage())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getUserRoles(GetUserRolesRequest request, StreamObserver<GetUserRolesResponse> responseObserver) {
        log.debug("Consulta de roles do usuário solicitada: {}", request.getUserId());
        
        try {
            Optional<User> userOpt = userRepository.findById(Long.parseLong(request.getUserId()));
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                List<String> roles = user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList());
                
                GetUserRolesResponse response = GetUserRolesResponse.newBuilder()
                    .addAllRoles(roles)
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                
                log.debug("Roles do usuário {}: {}", request.getUserId(), roles);
            } else {
                GetUserRolesResponse response = GetUserRolesResponse.newBuilder()
                    .setErrorMessage("Usuário não encontrado")
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (NumberFormatException e) {
            log.error("ID de usuário inválido: {}", request.getUserId(), e);
            
            GetUserRolesResponse response = GetUserRolesResponse.newBuilder()
                .setErrorMessage("ID de usuário inválido")
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Erro ao consultar roles do usuário", e);
            
            GetUserRolesResponse response = GetUserRolesResponse.newBuilder()
                .setErrorMessage("Erro interno: " + e.getMessage())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void healthCheck(HealthCheckRequest request, StreamObserver<HealthCheckResponse> responseObserver) {
        log.debug("Health check solicitado pelo serviço: {}", request.getServiceName());
        
        try {
            HealthCheckResponse response = HealthCheckResponse.newBuilder()
                .setHealthy(true)
                .setMessage("Auth service is healthy")
                .setTimestamp(Instant.now().getEpochSecond())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
            log.debug("Health check respondido com sucesso");
        } catch (Exception e) {
            log.error("Erro no health check", e);
            
            HealthCheckResponse response = HealthCheckResponse.newBuilder()
                .setHealthy(false)
                .setMessage("Auth service is unhealthy: " + e.getMessage())
                .setTimestamp(Instant.now().getEpochSecond())
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}