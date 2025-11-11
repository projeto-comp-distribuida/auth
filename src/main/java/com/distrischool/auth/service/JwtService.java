package com.distrischool.auth.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.distrischool.auth.security.Auth0JwtValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Serviço JWT que fornece uma interface simplificada para validação e extração de informações de tokens.
 * Usado principalmente pelo serviço gRPC para validação de tokens entre microsserviços.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final Auth0JwtValidator auth0JwtValidator;

    /**
     * Verifica se um token JWT é válido
     * 
     * @param token O token JWT a ser validado
     * @return true se o token é válido, false caso contrário
     */
    public boolean isTokenValid(String token) {
        try {
            auth0JwtValidator.validateToken(token);
            return true;
        } catch (JWTVerificationException e) {
            log.debug("Token JWT inválido: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Erro ao validar token JWT", e);
            return false;
        }
    }

    /**
     * Extrai o ID do usuário do token (Auth0 subject)
     * 
     * @param token O token JWT
     * @return O ID do usuário ou null se inválido
     */
    public String extractUserId(String token) {
        try {
            DecodedJWT jwt = auth0JwtValidator.validateToken(token);
            return auth0JwtValidator.getAuth0Id(jwt);
        } catch (Exception e) {
            log.debug("Erro ao extrair user ID do token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrai o email do usuário do token
     * 
     * @param token O token JWT
     * @return O email do usuário ou null se inválido
     */
    public String extractEmail(String token) {
        try {
            DecodedJWT jwt = auth0JwtValidator.validateToken(token);
            return auth0JwtValidator.getEmail(jwt);
        } catch (Exception e) {
            log.debug("Erro ao extrair email do token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrai o username do usuário do token
     * 
     * @param token O token JWT
     * @return O username (email) do usuário ou null se inválido
     */
    public String extractUsername(String token) {
        try {
            DecodedJWT jwt = auth0JwtValidator.validateToken(token);
            return auth0JwtValidator.getEmail(jwt); // Usando email como username
        } catch (Exception e) {
            log.debug("Erro ao extrair username do token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrai as roles do usuário do token
     * 
     * @param token O token JWT
     * @return Lista de roles do usuário ou lista vazia se inválido
     */
    public List<String> extractRoles(String token) {
        try {
            DecodedJWT jwt = auth0JwtValidator.validateToken(token);
            return auth0JwtValidator.getRoles(jwt);
        } catch (Exception e) {
            log.debug("Erro ao extrair roles do token: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Extrai a data de expiração do token
     * 
     * @param token O token JWT
     * @return A data de expiração ou null se inválido
     */
    public Date extractExpiration(String token) {
        try {
            DecodedJWT jwt = auth0JwtValidator.validateToken(token);
            return jwt.getExpiresAt();
        } catch (Exception e) {
            log.debug("Erro ao extrair expiração do token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrai o nome do usuário do token
     * 
     * @param token O token JWT
     * @return O nome do usuário ou null se inválido
     */
    public String extractName(String token) {
        try {
            DecodedJWT jwt = auth0JwtValidator.validateToken(token);
            return auth0JwtValidator.getName(jwt);
        } catch (Exception e) {
            log.debug("Erro ao extrair nome do token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Valida um token e retorna o DecodedJWT se válido
     * 
     * @param token O token JWT
     * @return O DecodedJWT se válido, null caso contrário
     */
    public DecodedJWT validateAndDecode(String token) {
        try {
            return auth0JwtValidator.validateToken(token);
        } catch (Exception e) {
            log.debug("Erro ao validar e decodificar token: {}", e.getMessage());
            return null;
        }
    }
}
