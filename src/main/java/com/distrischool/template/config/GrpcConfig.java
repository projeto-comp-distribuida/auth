package com.distrischool.template.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuração do gRPC.
 * Habilita o servidor e cliente gRPC para comunicação entre microsserviços.
 */
@Configuration
public class GrpcConfig {
    
    // A configuração do gRPC é feita automaticamente pelo spring-boot-starter-grpc
    // através das anotações @GrpcService e @GrpcClient
    
    // Configurações adicionais podem ser adicionadas aqui se necessário
}
