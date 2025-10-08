package com.distrischool.template.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento base para comunicação entre microsserviços do DistriSchool.
 * Todos os eventos do sistema devem estender esta classe base.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistriSchoolEvent {

    private String eventId;
    private String eventType;
    private String source;
    private String version;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
    private Map<String, Object> metadata;

    /**
     * Cria um evento básico do DistriSchool
     */
    public static DistriSchoolEvent create(String eventType, String source, Map<String, Object> data) {
        DistriSchoolEvent event = new DistriSchoolEvent();
        event.setEventId(java.util.UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setSource(source);
        event.setVersion("1.0");
        event.setTimestamp(LocalDateTime.now());
        event.setData(data);
        return event;
    }

    /**
     * Adiciona metadados ao evento
     */
    public DistriSchoolEvent withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
}
