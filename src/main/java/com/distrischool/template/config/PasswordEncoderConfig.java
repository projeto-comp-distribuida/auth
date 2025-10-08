package com.distrischool.template.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração do encoder de senhas.
 * Utiliza BCrypt para criptografia segura de senhas.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Bean do PasswordEncoder usando BCrypt com strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

