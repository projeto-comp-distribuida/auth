package com.distrischool.template.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidade base para o DistriSchool.
 * Esta classe serve como template para todas as entidades do sistema de gestão escolar.
 * Inclui campos comuns como auditoria, soft delete e timestamps.
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 255)
    private String deletedBy;

    /**
     * Verifica se a entidade foi excluída logicamente
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Marca a entidade como excluída logicamente
     */
    public void markAsDeleted(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Restaura a entidade (remove exclusão lógica)
     */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }
}
