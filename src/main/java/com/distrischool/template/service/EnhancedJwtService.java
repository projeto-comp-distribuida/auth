package com.distrischool.template.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.distrischool.template.config.Auth0Config;
import com.distrischool.template.entity.User;
import com.distrischool.template.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço que gera tokens JWT enriquecidos com informações do Auth0 + DB.
 * 
 * Este serviço:
 * 1. Decodifica o token Auth0 original
 * 2. Busca roles do banco de dados usando auth0_id
 * 3. Mapeia roles para permissions
 * 4. Cria um novo token JWT contendo todos os claims do Auth0 + roles e permissions do DB
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedJwtService {

    private final Auth0Config auth0Config;
    private final PermissionService permissionService;

    @Value("${app.jwt.secret:default-secret-key-change-in-production}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-hours:24}")
    private int expirationHours;

    /**
     * Gera um token JWT enriquecido com informações do Auth0 + roles/permissions do DB.
     * 
     * @param auth0Token O token original do Auth0
     * @param user O usuário do banco de dados com suas roles
     * @return Um novo token JWT contendo todos os claims do Auth0 + roles/permissions do DB
     */
    public String generateEnhancedToken(String auth0Token, User user) {
        try {
            // 1. Decodifica o token Auth0 (sem validar, pois já foi validado antes)
            DecodedJWT decodedAuth0Token = JWT.decode(auth0Token);
            
            // 2. Extrai claims do token Auth0
            String issuer = decodedAuth0Token.getIssuer();
            List<String> audienceList = decodedAuth0Token.getAudience();
            String audience = (audienceList != null && !audienceList.isEmpty()) 
                ? audienceList.get(0) 
                : auth0Config.getAudience();
            String subject = decodedAuth0Token.getSubject(); // auth0_id
            Date issuedAt = decodedAuth0Token.getIssuedAt();
            Date expiresAt = decodedAuth0Token.getExpiresAt();
            
            // 3. Extrai roles do banco de dados
            Set<UserRole> userRoles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());
            
            List<String> roles = userRoles.stream()
                .map(UserRole::name)
                .collect(Collectors.toList());
            
            // 4. Mapeia roles para permissions
            List<String> permissions = permissionService.getPermissionsFromRoles(userRoles);
            
            // 5. Extrai scope do token Auth0 (se existir)
            String scope = decodedAuth0Token.getClaim("scope") != null 
                ? decodedAuth0Token.getClaim("scope").asString() 
                : "openid profile email";
            
            // 6. Cria um novo token JWT com todos os claims
            Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
            
            Instant now = Instant.now();
            Instant expiration = expiresAt != null 
                ? expiresAt.toInstant() 
                : now.plusSeconds(expirationHours * 3600); // Usa expiração do Auth0 ou padrão
            
            return JWT.create()
                .withIssuer(issuer != null ? issuer : String.format("https://%s/", auth0Config.getDomain()))
                .withAudience(audience)
                .withSubject(subject) // auth0_id
                .withIssuedAt(Date.from(issuedAt != null ? issuedAt.toInstant() : now))
                .withExpiresAt(Date.from(expiration))
                // Preserva claims do Auth0
                .withClaim("email", getClaimValue(decodedAuth0Token.getClaim("email")))
                .withClaim("name", getClaimValue(decodedAuth0Token.getClaim("name")))
                .withClaim("nickname", getClaimValue(decodedAuth0Token.getClaim("nickname")))
                .withClaim("picture", getClaimValue(decodedAuth0Token.getClaim("picture")))
                .withClaim("scope", scope)
                // Adiciona roles e permissions do DB
                .withClaim("roles", roles)
                .withClaim("permissions", permissions)
                .sign(algorithm);
                
        } catch (Exception e) {
            log.error("Erro ao gerar token enriquecido: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar token enriquecido", e);
        }
    }

    /**
     * Helper para extrair valor de claim de forma segura.
     */
    private String getClaimValue(Claim claim) {
        if (claim == null || claim.isNull()) {
            return null;
        }
        return claim.asString();
    }
}

