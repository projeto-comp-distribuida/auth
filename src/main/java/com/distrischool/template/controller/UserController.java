package com.distrischool.template.controller;

import com.distrischool.template.dto.ApiResponse;
import com.distrischool.template.entity.User;
import com.distrischool.template.entity.UserRole;
import com.distrischool.template.service.UserService;
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
}

