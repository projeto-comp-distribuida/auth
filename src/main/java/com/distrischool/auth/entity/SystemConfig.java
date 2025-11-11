package com.distrischool.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade para configurações do sistema
 * Demonstra como gerenciar configurações de forma persistente
 */
@Entity
@Table(name = "system_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 255)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    /**
     * Verifica se a configuração está ativa
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Desativa a configuração
     */
    public void deactivate(String updatedBy) {
        this.active = false;
        this.updatedBy = updatedBy;
    }

    /**
     * Ativa a configuração
     */
    public void activate(String updatedBy) {
        this.active = true;
        this.updatedBy = updatedBy;
    }
}
