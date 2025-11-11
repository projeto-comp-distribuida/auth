package com.distrischool.auth.repository;

import com.distrischool.auth.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para configurações do sistema
 * Demonstra como gerenciar configurações de forma persistente
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * Busca configuração por chave
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * Busca configuração por chave (case insensitive)
     */
    Optional<SystemConfig> findByConfigKeyIgnoreCase(String configKey);

    /**
     * Busca configurações ativas
     */
    List<SystemConfig> findByActiveTrue();

    /**
     * Busca configurações inativas
     */
    List<SystemConfig> findByActiveFalse();

    /**
     * Verifica se existe configuração com a chave especificada
     */
    boolean existsByConfigKey(String configKey);

    /**
     * Verifica se existe configuração ativa com a chave especificada
     */
    @Query("SELECT COUNT(s) > 0 FROM SystemConfig s WHERE s.configKey = :configKey AND s.active = true")
    boolean existsActiveByConfigKey(@Param("configKey") String configKey);

    /**
     * Busca configurações por usuário que criou
     */
    List<SystemConfig> findByCreatedBy(String createdBy);

    /**
     * Busca configurações por usuário que fez a última atualização
     */
    List<SystemConfig> findByUpdatedBy(String updatedBy);

    /**
     * Busca configurações que contêm uma palavra-chave na descrição
     */
    @Query("SELECT s FROM SystemConfig s WHERE s.description LIKE %:keyword%")
    List<SystemConfig> findByDescriptionContaining(@Param("keyword") String keyword);

    /**
     * Busca configurações por prefixo da chave
     */
    @Query("SELECT s FROM SystemConfig s WHERE s.configKey LIKE :prefix%")
    List<SystemConfig> findByConfigKeyStartingWith(@Param("prefix") String prefix);

    /**
     * Busca configurações ativas por prefixo da chave
     */
    @Query("SELECT s FROM SystemConfig s WHERE s.configKey LIKE :prefix% AND s.active = true")
    List<SystemConfig> findActiveByConfigKeyStartingWith(@Param("prefix") String prefix);
}
