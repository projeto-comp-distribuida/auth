package com.distrischool.auth.service;

import com.distrischool.auth.entity.SystemConfig;
import com.distrischool.auth.repository.SystemConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Serviço para gerenciar configurações do sistema com cache Redis
 * Demonstra o uso do Redis para cache e operações de alta performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CONFIG_CACHE_PREFIX = "config:";
    private static final String CONFIG_LIST_CACHE_KEY = "config:all";
    private static final Duration DEFAULT_CACHE_TTL = Duration.ofHours(1);

    /**
     * Busca uma configuração por chave com cache Redis
     */
    @Cacheable(value = "systemConfig", key = "#configKey")
    public Optional<SystemConfig> getConfigByKey(String configKey) {
        log.debug("Buscando configuração: {}", configKey);
        return systemConfigRepository.findByConfigKey(configKey);
    }

    /**
     * Busca uma configuração por chave (case insensitive) com cache Redis
     */
    @Cacheable(value = "systemConfig", key = "#configKey.toLowerCase()")
    public Optional<SystemConfig> getConfigByKeyIgnoreCase(String configKey) {
        log.debug("Buscando configuração (case insensitive): {}", configKey);
        return systemConfigRepository.findByConfigKeyIgnoreCase(configKey);
    }

    /**
     * Busca o valor de uma configuração diretamente
     */
    public Optional<String> getConfigValue(String configKey) {
        return getConfigByKey(configKey)
                .filter(SystemConfig::isActive)
                .map(SystemConfig::getConfigValue);
    }

    /**
     * Busca o valor de uma configuração com valor padrão
     */
    public String getConfigValue(String configKey, String defaultValue) {
        return getConfigValue(configKey).orElse(defaultValue);
    }

    /**
     * Busca todas as configurações ativas com cache Redis
     */
    @Cacheable(value = "systemConfigList")
    public List<SystemConfig> getAllActiveConfigs() {
        log.debug("Buscando todas as configurações ativas");
        return systemConfigRepository.findByActiveTrue();
    }

    /**
     * Salva uma nova configuração e limpa o cache
     */
    @CacheEvict(value = {"systemConfig", "systemConfigList"}, allEntries = true)
    public SystemConfig saveConfig(SystemConfig config) {
        log.info("Salvando configuração: {}", config.getConfigKey());
        return systemConfigRepository.save(config);
    }

    /**
     * Atualiza uma configuração existente e limpa o cache
     */
    @CacheEvict(value = {"systemConfig", "systemConfigList"}, allEntries = true)
    public SystemConfig updateConfig(SystemConfig config) {
        log.info("Atualizando configuração: {}", config.getConfigKey());
        return systemConfigRepository.save(config);
    }

    /**
     * Atualiza o valor de uma configuração
     */
    @CacheEvict(value = {"systemConfig", "systemConfigList"}, allEntries = true)
    public Optional<SystemConfig> updateConfigValue(String configKey, String newValue, String updatedBy) {
        log.info("Atualizando valor da configuração: {} = {}", configKey, newValue);
        return systemConfigRepository.findByConfigKey(configKey)
                .map(config -> {
                    config.setConfigValue(newValue);
                    config.setUpdatedBy(updatedBy);
                    return systemConfigRepository.save(config);
                });
    }

    /**
     * Desativa uma configuração
     */
    @CacheEvict(value = {"systemConfig", "systemConfigList"}, allEntries = true)
    public Optional<SystemConfig> deactivateConfig(String configKey, String updatedBy) {
        log.info("Desativando configuração: {}", configKey);
        return systemConfigRepository.findByConfigKey(configKey)
                .map(config -> {
                    config.deactivate(updatedBy);
                    return systemConfigRepository.save(config);
                });
    }

    /**
     * Ativa uma configuração
     */
    @CacheEvict(value = {"systemConfig", "systemConfigList"}, allEntries = true)
    public Optional<SystemConfig> activateConfig(String configKey, String updatedBy) {
        log.info("Ativando configuração: {}", configKey);
        return systemConfigRepository.findByConfigKey(configKey)
                .map(config -> {
                    config.activate(updatedBy);
                    return systemConfigRepository.save(config);
                });
    }

    /**
     * Limpa todo o cache de configurações
     */
    public void clearConfigCache() {
        log.info("Limpando cache de configurações");
        redisTemplate.delete(redisTemplate.keys(CONFIG_CACHE_PREFIX + "*"));
        redisTemplate.delete(CONFIG_LIST_CACHE_KEY);
    }

    /**
     * Define um valor no Redis diretamente (para casos especiais)
     */
    public void setRedisValue(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
        log.debug("Valor definido no Redis: {} = {}", key, value);
    }

    /**
     * Busca um valor do Redis diretamente
     */
    public Optional<Object> getRedisValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        log.debug("Valor buscado do Redis: {} = {}", key, value);
        return Optional.ofNullable(value);
    }

    /**
     * Remove um valor do Redis
     */
    public void deleteRedisValue(String key) {
        redisTemplate.delete(key);
        log.debug("Valor removido do Redis: {}", key);
    }

    /**
     * Verifica se uma chave existe no Redis
     */
    public boolean existsInRedis(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        log.debug("Verificando existência no Redis: {} = {}", key, exists);
        return exists != null && exists;
    }
}
