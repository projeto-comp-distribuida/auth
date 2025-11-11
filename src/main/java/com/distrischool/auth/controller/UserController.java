package com.distrischool.auth.controller;

import com.distrischool.auth.dto.ApiResponse;
import com.distrischool.auth.entity.User;
import com.distrischool.auth.entity.UserRole;
import com.distrischool.auth.service.UserService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller de gerenciamento de usuários - Padrão MVC.
 * Gerencia operações CRUD de usuários (apenas para admins).
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserService userService;

    /**
     * Busca usuário por ID
     * Requer autenticação
     */
    @GetMapping("/{id}")
    @Timed(value = "users.get.by.id", description = "Time taken to get user by id")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        log.info("GET /api/v1/users/{}", id);
        
        User user = userService.findById(id);
        
        return ResponseEntity.ok(
            ApiResponse.<User>builder()
                .success(true)
                .message("Usuário encontrado")
                .data(user)
                .build()
        );
    }

    /**
     * Busca usuário por email
     * Apenas admins podem buscar por email
     */
    @GetMapping("/email/{email}")
    @Timed(value = "users.get.by.email", description = "Time taken to get user by email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        log.info("GET /api/v1/users/email/{}", email);
        
        User user = userService.findByEmail(email);
        
        return ResponseEntity.ok(
            ApiResponse.<User>builder()
                .success(true)
                .message("Usuário encontrado")
                .data(user)
                .build()
        );
    }

    /**
     * Lista todos os usuários ativos
     * Apenas admins
     */
    @GetMapping
    @Timed(value = "users.get.all", description = "Time taken to get all users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        log.info("GET /api/v1/users");
        
        List<User> users = userService.findAllActiveUsers();
        
        return ResponseEntity.ok(
            ApiResponse.<List<User>>builder()
                .success(true)
                .message("Lista de usuários")
                .data(users)
                .build()
        );
    }

    /**
     * Busca usuários por role
     * Apenas admins e professores
     */
    @GetMapping("/role/{role}")
    @Timed(value = "users.get.by.role", description = "Time taken to get users by role")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable UserRole role) {
        log.info("GET /api/v1/users/role/{}", role);
        
        List<User> users = userService.findByRole(role);
        
        return ResponseEntity.ok(
            ApiResponse.<List<User>>builder()
                .success(true)
                .message("Usuários com role " + role.getDescription())
                .data(users)
                .build()
        );
    }

    /**
     * Desativa um usuário
     * Apenas admins
     */
    @DeleteMapping("/{id}")
    @Timed(value = "users.delete", description = "Time taken to delete user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "ADMIN") String deletedBy) {
        log.info("DELETE /api/v1/users/{}", id);
        
        userService.deactivateUser(id, deletedBy);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Usuário desativado com sucesso")
                .build()
        );
    }

    /**
     * Reativa um usuário
     * Apenas admins
     */
    @PatchMapping("/{id}/reactivate")
    @Timed(value = "users.reactivate", description = "Time taken to reactivate user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reactivateUser(@PathVariable Long id) {
        log.info("PATCH /api/v1/users/{}/reactivate", id);
        
        userService.reactivateUser(id);
        
        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .success(true)
                .message("Usuário reativado com sucesso")
                .build()
        );
    }

    /**
     * Busca usuário por Auth0 ID
     * Endpoint interno para uso entre serviços
     */
    @GetMapping("/auth0/{auth0Id}")
    @Timed(value = "users.get.by.auth0id", description = "Time taken to get user by auth0 id")
    public ResponseEntity<ApiResponse<User>> getUserByAuth0Id(@PathVariable String auth0Id) {
        log.info("GET /api/v1/users/auth0/{}", auth0Id);
        
        User user = userService.findByAuth0Id(auth0Id);
        
        return ResponseEntity.ok(
            ApiResponse.<User>builder()
                .success(true)
                .message("Usuário encontrado")
                .data(user)
                .build()
        );
    }

    /**
     * Verifica se o usuário tem uma role específica
     * Endpoint interno para uso entre serviços
     */
    @GetMapping("/{id}/has-role")
    @Timed(value = "users.check.role", description = "Time taken to check user role")
    public ResponseEntity<ApiResponse<Boolean>> hasRole(
            @PathVariable Long id,
            @RequestParam String role) {
        log.info("GET /api/v1/users/{}/has-role?role={}", id, role);
        
        User user = userService.findById(id);
        
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                    .success(true)
                    .message("Role inválida")
                    .data(false)
                    .build()
            );
        }
        
        boolean hasRole = user.hasRole(userRole);
        
        return ResponseEntity.ok(
            ApiResponse.<Boolean>builder()
                .success(true)
                .message("Verificação de role realizada")
                .data(hasRole)
                .build()
        );
    }
}

