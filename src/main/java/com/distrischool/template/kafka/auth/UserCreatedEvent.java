package com.distrischool.template.kafka.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evento publicado quando um novo usuário é criado no sistema de autenticação.
 * Outros microsserviços podem usar este evento para sincronizar dados de usuário.
 */
public class UserCreatedEvent extends AuthEvent {
    
    public UserCreatedEvent() {
        super("user.created", new HashMap<>());
    }
    
    public UserCreatedEvent(Map<String, Object> data) {
        super("user.created", data);
    }
    
    /**
     * Cria um evento de usuário criado
     */
    public static UserCreatedEvent create(String userId, String email, String fullName, 
                                        String firstName, String lastName, String phone,
                                        String documentNumber, List<String> roles, 
                                        String createdAt) {
        Map<String, Object> data = Map.of(
            "userId", userId,
            "email", email,
            "fullName", fullName,
            "firstName", firstName,
            "lastName", lastName,
            "phone", phone != null ? phone : "",
            "documentNumber", documentNumber != null ? documentNumber : "",
            "roles", roles,
            "createdAt", createdAt,
            "active", true
        );
        
        return new UserCreatedEvent(data);
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
     * Obtém a data de criação
     */
    public String getCreatedAt() {
        return data != null ? (String) data.get("createdAt") : null;
    }
    
    /**
     * Verifica se o usuário está ativo
     */
    public Boolean isActive() {
        return data != null ? (Boolean) data.get("active") : null;
    }
}
