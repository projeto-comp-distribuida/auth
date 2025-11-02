package com.distrischool.template.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.distrischool.template.config.Auth0Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validador para tokens JWT enriquecidos gerados pelo EnhancedJwtService.
 * 
 * Este validador verifica tokens assinados com nossa própria secret key
 * que contêm claims do Auth0 + roles/permissions do banco de dados.
 */
@Component
@Slf4j
public class EnhancedJwtValidator {

    private final String jwtSecret;
    private final String expectedIssuer;
    private final String expectedAudience;

    public EnhancedJwtValidator(
            Auth0Config auth0Config,
            @Value("${app.jwt.secret:default-secret-key-change-in-production-minimum-256-bits}") String jwtSecret
    ) {
        this.jwtSecret = jwtSecret;
        this.expectedIssuer = String.format("https://%s/", auth0Config.getDomain());
        this.expectedAudience = auth0Config.getAudience();
    }

    /**
     * Valida um token JWT enriquecido
     */
    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            
            JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(expectedIssuer)
                .withAudience(expectedAudience)
                .build();
            
            DecodedJWT decodedJWT = verifier.verify(token);
            
            log.debug("Token enriquecido validado com sucesso: sub={}", decodedJWT.getSubject());
            return decodedJWT;
            
        } catch (JWTVerificationException e) {
            log.warn("Falha ao validar token enriquecido: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro ao processar token enriquecido", e);
            throw new JWTVerificationException("Erro ao processar token", e);
        }
    }

    /**
     * Extrai o Auth0 ID (sub claim) do token
     */
    public String getAuth0Id(DecodedJWT jwt) {
        return jwt.getSubject();
    }

    /**
     * Extrai o email do token
     */
    public String getEmail(DecodedJWT jwt) {
        return jwt.getClaim("email").asString();
    }

    /**
     * Extrai roles do token
     */
    public List<String> getRoles(DecodedJWT jwt) {
        List<String> roles = jwt.getClaim("roles").asList(String.class);
        return roles != null ? roles : List.of();
    }

    /**
     * Extrai permissions do token
     */
    public List<String> getPermissions(DecodedJWT jwt) {
        List<String> permissions = jwt.getClaim("permissions").asList(String.class);
        return permissions != null ? permissions : List.of();
    }
}

