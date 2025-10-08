package com.distrischool.template.kafka.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evento publicado quando um usuário faz login no sistema.
 * Outros microsserviços podem usar este evento para auditoria e sincronização de sessões.
 */
public class UserLoggedEvent extends AuthEvent {
    
    public UserLoggedEvent() {
        super("user.logged", new HashMap<>());
    }
    
    public UserLoggedEvent(Map<String, Object> data) {
        super("user.logged", data);
    }
    
    /**
     * Cria um evento de login de usuário
     */
    public static UserLoggedEvent create(String userId, String email, String fullName, 
                                       List<String> roles, String loginTime, String ipAddress) {
        Map<String, Object> data = Map.of(
            "userId", userId,
            "email", email,
            "fullName", fullName,
            "roles", roles,
            "loginTime", loginTime,
            "ipAddress", ipAddress != null ? ipAddress : "unknown",
            "sessionId", java.util.UUID.randomUUID().toString()
        );
        
        return new UserLoggedEvent(data);
    }
    
    /**
     * Obtém o nome completo do usuário
     */
    public String getFullName() {
        return data != null ? (String) data.get("fullName") : null;
    }
    
    /**
     * Obtém as roles do usuário
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles() {
        return data != null ? (List<String>) data.get("roles") : null;
    }
    
    /**
     * Obtém o horário do login
     */
    public String getLoginTime() {
        return data != null ? (String) data.get("loginTime") : null;
    }
    
    /**
     * Obtém o endereço IP do login
     */
    public String getIpAddress() {
        return data != null ? (String) data.get("ipAddress") : null;
    }
    
    /**
     * Obtém o ID da sessão
     */
    public String getSessionId() {
        return data != null ? (String) data.get("sessionId") : null;
    }
}
