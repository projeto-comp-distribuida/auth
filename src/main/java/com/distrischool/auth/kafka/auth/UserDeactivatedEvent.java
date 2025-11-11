package com.distrischool.auth.kafka.auth;


import java.util.HashMap;
import java.util.Map;

/**
 * Evento publicado quando um usuário é desativado no sistema de autenticação.
 * Outros microsserviços podem usar este evento para invalidar sessões e limpar dados.
 */
public class UserDeactivatedEvent extends AuthEvent {
    
    public UserDeactivatedEvent() {
        super("user.deactivated", new HashMap<>());
    }
    
    public UserDeactivatedEvent(Map<String, Object> data) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventType = "user.deactivated";
        this.source = "auth-service";
        this.version = "1.0";
        this.timestamp = java.time.LocalDateTime.now();
        this.data = data;
    }
    
    /**
     * Cria um evento de usuário desativado
     */
    public static UserDeactivatedEvent create(String userId, String email, String fullName, 
                                            String deactivatedAt, String reason) {
        Map<String, Object> data = Map.of(
            "userId", userId,
            "email", email,
            "fullName", fullName,
            "deactivatedAt", deactivatedAt,
            "reason", reason != null ? reason : "User deactivated",
            "active", false
        );
        
        return new UserDeactivatedEvent(data);
    }
    
    /**
     * Obtém o nome completo do usuário
     */
    public String getFullName() {
        return data != null ? (String) data.get("fullName") : null;
    }
    
    /**
     * Obtém a data de desativação
     */
    public String getDeactivatedAt() {
        return data != null ? (String) data.get("deactivatedAt") : null;
    }
    
    /**
     * Obtém o motivo da desativação
     */
    public String getReason() {
        return data != null ? (String) data.get("reason") : null;
    }
    
    /**
     * Verifica se o usuário está ativo (sempre false para este evento)
     */
    public Boolean isActive() {
        return data != null ? (Boolean) data.get("active") : false;
    }
}
