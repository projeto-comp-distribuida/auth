package com.distrischool.auth.kafka.auth;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Evento base para operações de autenticação no DistriSchool.
 * Todos os eventos relacionados à autenticação devem estender esta classe.
 */
public class AuthEvent {
    
    protected String eventId;
    protected String eventType;
    protected String source;
    protected String version;
    protected LocalDateTime timestamp;
    protected Map<String, Object> data;
    protected Map<String, Object> metadata;
    
    // Default constructor
    public AuthEvent() {
    }
    
    // Constructor with all fields
    public AuthEvent(String eventId, String eventType, String source, String version, 
                    LocalDateTime timestamp, Map<String, Object> data, Map<String, Object> metadata) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.source = source;
        this.version = version;
        this.timestamp = timestamp;
        this.data = data;
        this.metadata = metadata;
    }
    
    /**
     * Cria um evento de autenticação básico
     */
    protected AuthEvent(String eventType, Map<String, Object> data) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.source = "auth-service";
        this.version = "1.0";
        this.timestamp = LocalDateTime.now();
        this.data = data;
    }
    
    /**
     * Adiciona metadados ao evento
     */
    public AuthEvent withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
    
    /**
     * Obtém o ID do usuário do evento
     */
    public String getUserId() {
        return data != null ? (String) data.get("userId") : null;
    }
    
    /**
     * Obtém o email do usuário do evento
     */
    public String getUserEmail() {
        return data != null ? (String) data.get("email") : null;
    }
    
    // Getter and setter methods
    public String getEventId() {
        return eventId;
    }
    
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthEvent authEvent = (AuthEvent) o;
        return java.util.Objects.equals(eventId, authEvent.eventId);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(eventId);
    }
    
    @Override
    public String toString() {
        return "AuthEvent{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", source='" + source + '\'' +
                ", version='" + version + '\'' +
                ", timestamp=" + timestamp +
                ", data=" + data +
                ", metadata=" + metadata +
                '}';
    }
}
