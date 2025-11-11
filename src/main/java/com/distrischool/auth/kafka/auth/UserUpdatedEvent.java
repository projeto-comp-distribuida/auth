package com.distrischool.auth.kafka.auth;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evento publicado quando um usuário é atualizado no sistema de autenticação.
 * Outros microsserviços podem usar este evento para sincronizar mudanças de dados.
 */
public class UserUpdatedEvent extends AuthEvent {
    
    public UserUpdatedEvent() {
        super("user.updated", new HashMap<>());
    }
    
    public UserUpdatedEvent(Map<String, Object> data) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.eventType = "user.updated";
        this.source = "auth-service";
        this.version = "1.0";
        this.timestamp = java.time.LocalDateTime.now();
        this.data = data;
    }
    
    /**
     * Cria um evento de usuário atualizado
     */
    public static UserUpdatedEvent create(String userId, String email, String fullName, 
                                        String firstName, String lastName, String phone,
                                        String documentNumber, List<String> roles, 
                                        Boolean active, String updatedAt) {
        Map<String, Object> data = Map.of(
            "userId", userId,
            "email", email,
            "fullName", fullName,
            "firstName", firstName,
            "lastName", lastName,
            "phone", phone != null ? phone : "",
            "documentNumber", documentNumber != null ? documentNumber : "",
            "roles", roles,
            "active", active,
            "updatedAt", updatedAt
        );
        
        return new UserUpdatedEvent(data);
    }
    
    /**
     * Obtém o nome completo do usuário
     */
    public String getFullName() {
        return data != null ? (String) data.get("fullName") : null;
    }
    
    /**
     * Obtém o primeiro nome do usuário
     */
    public String getFirstName() {
        return data != null ? (String) data.get("firstName") : null;
    }
    
    /**
     * Obtém o último nome do usuário
     */
    public String getLastName() {
        return data != null ? (String) data.get("lastName") : null;
    }
    
    /**
     * Obtém o telefone do usuário
     */
    public String getPhone() {
        return data != null ? (String) data.get("phone") : null;
    }
    
    /**
     * Obtém o número do documento do usuário
     */
    public String getDocumentNumber() {
        return data != null ? (String) data.get("documentNumber") : null;
    }
    
    /**
     * Obtém as roles do usuário
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles() {
        return data != null ? (List<String>) data.get("roles") : null;
    }
    
    /**
     * Verifica se o usuário está ativo
     */
    public Boolean isActive() {
        return data != null ? (Boolean) data.get("active") : null;
    }
    
    /**
     * Obtém a data de atualização
     */
    public String getUpdatedAt() {
        return data != null ? (String) data.get("updatedAt") : null;
    }
}
