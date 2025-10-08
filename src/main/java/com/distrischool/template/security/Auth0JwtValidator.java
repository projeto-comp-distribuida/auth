package com.distrischool.template.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.distrischool.template.config.Auth0Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Valida tokens JWT emitidos pelo Auth0.
 * 
 * Auth0 usa RSA256 (assimétrico) com chaves públicas disponíveis via JWK endpoint.
 * Este validador:
 * 1. Busca a chave pública do Auth0 via JWK
 * 2. Valida a assinatura do token
 * 3. Valida issuer, audience, expiração
 */
@Component
@Slf4j
public class Auth0JwtValidator {

    private final JwkProvider jwkProvider;
    private final String audience;
    private final String issuer;

    public Auth0JwtValidator(Auth0Config auth0Config) {
        // Auth0 JWK endpoint: https://{domain}/.well-known/jwks.json
        String jwkUrl = String.format("https://%s/", auth0Config.getDomain());
        
        // Cache JWKs por 10 minutos (para performance)
        this.jwkProvider = new JwkProviderBuilder(jwkUrl)
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build();
        
        this.audience = auth0Config.getAudience();
        this.issuer = String.format("https://%s/", auth0Config.getDomain());
        
        log.info("Auth0 JWT Validator inicializado: issuer={}, audience={}", issuer, audience);
    }

    /**
     * Valida um token JWT do Auth0
     */
    public DecodedJWT validateToken(String token) throws JWTVerificationException {
        try {
            // Decodifica o token (sem validação) para obter o kid (Key ID)
            DecodedJWT jwt = JWT.decode(token);
            
            // Busca a chave pública correspondente ao kid
            Jwk jwk = jwkProvider.get(jwt.getKeyId());
            
            // Obtém a chave pública RSA
            RSAPublicKey publicKey = (RSAPublicKey) jwk.getPublicKey();
            
            // Cria o algoritmo com a chave pública
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            
            // Valida o token com todas as verificações
            DecodedJWT verifiedJWT = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()
                .verify(token);
            
            log.debug("Token Auth0 validado com sucesso: sub={}", verifiedJWT.getSubject());
            return verifiedJWT;
            
        } catch (JWTVerificationException e) {
            log.warn("Falha ao validar token Auth0: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Erro ao processar token Auth0", e);
            throw new JWTVerificationException("Erro ao processar token", e);
        }
    }

    /**
     * Extrai informações do token sem validar (use com cuidado!)
     */
    public DecodedJWT decodeWithoutValidation(String token) {
        return JWT.decode(token);
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
     * Extrai roles do token (custom claim)
     * Auth0 pode armazenar roles em diferentes formatos:
     * - roles: ["admin", "teacher"]
     * - permissions: ["read:users", "write:users"]
     * - custom namespace: {"https://distrischool.com/roles": ["admin"]}
     */
    public List<String> getRoles(DecodedJWT jwt) {
        // Tenta diferentes locais comuns para roles
        
        // 1. Custom namespace (recomendado pelo Auth0)
        String namespace = "https://distrischool.com/roles";
        List<String> roles = jwt.getClaim(namespace).asList(String.class);
        if (roles != null && !roles.isEmpty()) {
            return roles;
        }
        
        // 2. Claim "roles" padrão
        roles = jwt.getClaim("roles").asList(String.class);
        if (roles != null && !roles.isEmpty()) {
            return roles;
        }
        
        // 3. App metadata
        roles = jwt.getClaim("app_metadata").asMap() != null
            ? (List<String>) jwt.getClaim("app_metadata").asMap().get("roles")
            : null;
        if (roles != null && !roles.isEmpty()) {
            return roles;
        }
        
        // Sem roles encontradas
        return List.of();
    }

    /**
     * Extrai o nome do usuário
     */
    public String getName(DecodedJWT jwt) {
        String name = jwt.getClaim("name").asString();
        if (name == null || name.isEmpty()) {
            // Fallback para nickname
            name = jwt.getClaim("nickname").asString();
        }
        return name;
    }
}

