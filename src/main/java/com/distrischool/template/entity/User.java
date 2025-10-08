package com.distrischool.template.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidade User representa os usuários do sistema DistriSchool.
 * Suporta 4 tipos de usuários: ADMIN, TEACHER, STUDENT, PARENT.
 * 
 * Auth0 Integration:
 * - Auth0 handles: passwords, email verification, password reset, MFA, login attempts
 * - This entity stores: user profile, roles, and sync with Auth0
 * - All authentication is managed by Auth0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_auth0_id", columnList = "auth0_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"roles"})
@ToString(exclude = {"roles"})
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;


    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "document_number", unique = true, length = 50)
    private String documentNumber;

    /**
     * Auth0 User ID (sub claim from Auth0 token)
     * Format: "auth0|xxxxx" or "google-oauth2|xxxxx" etc
     */
    @Column(name = "auth0_id", unique = true, length = 255)
    private String auth0Id;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    /**
     * Retorna o nome completo do usuário
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Verifica se o usuário pode fazer login
     */
    public boolean canLogin() {
        return active;
    }

    /**
     * Atualiza o último login
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    /**
     * Adiciona uma role ao usuário
     */
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    /**
     * Remove uma role do usuário
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    /**
     * Verifica se o usuário tem uma role específica
     */
    public boolean hasRole(UserRole userRole) {
        return this.roles.stream()
            .anyMatch(role -> role.getName() == userRole);
    }

    /**
     * Verifica se o usuário usa Auth0
     */
    public boolean isAuth0User() {
        return auth0Id != null && !auth0Id.isEmpty();
    }
}

