package com.distrischool.auth.kafka.auth;

import java.util.HashMap;
import java.util.Map;

/**
 * Evento publicado quando a senha de um usuário é alterada.
 * Outros microsserviços podem usar este evento para invalidar sessões existentes.
 */
public class PasswordChangedEvent extends AuthEvent {
    
    public PasswordChangedEvent() {
        super("user.password.changed", new HashMap<>());
    }
    
    public PasswordChangedEvent(Map<String, Object> data) {
        super("user.password.changed", data);
    }
    
    /**
     * Cria um evento de senha alterada
     */
    public static PasswordChangedEvent create(String userId, String email, String fullName, 
                                            String changedAt, String ipAddress) {
        Map<String, Object> data = Map.of(
            "userId", userId,
            "email", email,
            "fullName", fullName,
            "changedAt", changedAt,
            "ipAddress", ipAddress != null ? ipAddress : "unknown",
            "requiresReauth", true
        );
        
        return new PasswordChangedEvent(data);
    }
    
    /**
     * Obtém o nome completo do usuário
     */
    public String getFullName() {
        return data != null ? (String) data.get("fullName") : null;
    }
    
    /**
     * Obtém a data da alteração da senha
     */
    public String getChangedAt() {
        return data != null ? (String) data.get("changedAt") : null;
    }
    
    /**
     * Obtém o endereço IP da alteração
     */
    public String getIpAddress() {
        return data != null ? (String) data.get("ipAddress") : null;
    }
    
    /**
     * Verifica se requer reautenticação
     */
    public Boolean requiresReauth() {
        return data != null ? (Boolean) data.get("requiresReauth") : true;
    }
}
