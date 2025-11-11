package com.distrischool.auth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração de integração com Auth0.
 * Armazena as credenciais e configurações do Auth0.
 */
@Configuration
@Getter
public class Auth0Config {

    @Value("${AUTH0_DOMAIN}")
    private String domain;

    @Value("${AUTH0_CLIENT_ID}")
    private String clientId;

    @Value("${AUTH0_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${AUTH0_AUDIENCE}")
    private String audience;

    @Value("${AUTH0_CONNECTION:Username-Password-Authentication}")
    private String connection;
}

