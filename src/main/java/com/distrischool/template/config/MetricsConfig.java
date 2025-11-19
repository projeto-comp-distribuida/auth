package com.distrischool.template.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração padrão para adicionar tags comuns às métricas do serviço de autenticação.
 */
@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> configureCommonTags(
        @Value("${spring.application.name:auth-service}") String applicationName,
        @Value("${SPRING_PROFILES_ACTIVE:local}") String activeProfile
    ) {
        return registry -> registry.config()
            .commonTags("service", applicationName, "environment", activeProfile);
    }
}


