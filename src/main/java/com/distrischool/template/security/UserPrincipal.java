package com.distrischool.template.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;

/**
 * Representa o usuário autenticado (Principal do Spring Security).
 * 
 * Contém informações básicas do usuário extraídas do JWT (Auth0 ou local).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements Principal {
    
    /**
     * ID do usuário no banco local (pode ser null se usuário não existir localmente)
     */
    private Long id;
    
    /**
     * Email do usuário
     */
    private String email;
    
    /**
     * Auth0 ID (sub claim) - null se for autenticação local
     */
    private String auth0Id;

    /**
     * Construtor de compatibilidade (local auth)
     */
    public UserPrincipal(Long id, String email) {
        this.id = id;
        this.email = email;
        this.auth0Id = null;
    }

    @Override
    public String getName() {
        return email;
    }

    /**
     * Verifica se o usuário é autenticado via Auth0
     */
    public boolean isAuth0User() {
        return auth0Id != null && !auth0Id.isEmpty();
    }
}

