package com.distrischool.template.dto.auth;

import com.distrischool.template.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO para resposta de autenticação.
 * Retorna informações do usuário após registro ou login.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private UserResponse user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private String documentNumber;
        private String auth0Id;
        private boolean active;
        private LocalDateTime lastLogin;
        private Set<UserRole> roles;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}