package com.distrischool.template.service;

import com.distrischool.template.config.Auth0Config;
import com.distrischool.template.entity.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço para interagir com a Auth0 Management API.
 * Permite criar, atualizar e gerenciar usuários no Auth0 programaticamente.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class Auth0ManagementService {

    private final Auth0Config auth0Config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Cria um novo usuário no Auth0
     */
    public Auth0User createUser(String email, String password, String firstName, String lastName, 
                               String phone, String documentNumber, UserRole role) {
        try {
            // Obter token de acesso para Management API
            String accessToken = getManagementApiToken();
            
            // Preparar dados do usuário
            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("password", password);
            userData.put("given_name", firstName);
            userData.put("family_name", lastName);
            userData.put("name", firstName + " " + lastName);
            userData.put("email_verified", false);
            userData.put("connection", auth0Config.getConnection());
            
            // Adicionar metadados customizados
            Map<String, Object> userMetadata = new HashMap<>();
            userMetadata.put("document_number", documentNumber);
            userMetadata.put("role", role.name());
            userData.put("user_metadata", userMetadata);
            
            // Fazer requisição para criar usuário
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userData, headers);
            
            String url = String.format("https://%s/api/v2/users", auth0Config.getDomain());
            log.info("Criando usuário no Auth0 - URL: {}", url);
            log.info("Dados do usuário: {}", userData);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            log.info("Resposta da criação de usuário - Status: {}", response.getStatusCode());
            log.info("Body da resposta: {}", response.getBody());
            
            if (response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode userNode = objectMapper.readTree(response.getBody());
                log.info("Usuário criado no Auth0: {}", email);
                
                return Auth0User.builder()
                    .auth0Id(userNode.get("user_id").asText())
                    .email(userNode.get("email").asText())
                    .firstName(userNode.get("given_name").asText())
                    .lastName(userNode.get("family_name").asText())
                    .emailVerified(userNode.get("email_verified").asBoolean())
                    .build();
            } else {
                log.error("Erro ao criar usuário no Auth0 - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Falha ao criar usuário no Auth0: " + response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Erro ao criar usuário no Auth0: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao criar usuário no Auth0", e);
        }
    }

    /**
     * Autentica um usuário e retorna um token de acesso Auth0
     */
    public Auth0LoginResponse login(String email, String password) {
        try {
            Map<String, String> loginData = new HashMap<>();
            loginData.put("client_id", auth0Config.getClientId());
            loginData.put("client_secret", auth0Config.getClientSecret());
            loginData.put("audience", auth0Config.getAudience());
            // Use password-realm grant type when specifying a realm/connection
            loginData.put("grant_type", "http://auth0.com/oauth/grant-type/password-realm");
            loginData.put("username", email);
            loginData.put("password", password);
            loginData.put("scope", "openid profile email");
            loginData.put("realm", auth0Config.getConnection());
            
            log.info("Tentando autenticar usuário: {} com conexão: {}", email, auth0Config.getConnection());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(loginData, headers);
            
            String url = String.format("https://%s/oauth/token", auth0Config.getDomain());
            log.debug("Auth0 login request: {}", loginData);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode tokenNode = objectMapper.readTree(response.getBody());
                log.info("Login bem-sucedido para usuário: {}", email);
                
                return Auth0LoginResponse.builder()
                    .accessToken(tokenNode.get("access_token").asText())
                    .tokenType(tokenNode.get("token_type").asText())
                    .expiresIn(tokenNode.get("expires_in").asInt())
                    .build();
            } else {
                log.error("Falha no login - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Credenciais inválidas");
            }
            
        } catch (Exception e) {
            log.error("Erro ao autenticar usuário: {}", e.getMessage());
            throw new RuntimeException("Falha na autenticação: " + e.getMessage());
        }
    }

    /**
     * Obtém token de acesso para a Management API
     */
    private String getManagementApiToken() {
        try {
            Map<String, String> tokenData = new HashMap<>();
            tokenData.put("client_id", auth0Config.getClientId());
            tokenData.put("client_secret", auth0Config.getClientSecret());
            tokenData.put("audience", String.format("https://%s/api/v2/", auth0Config.getDomain()));
            tokenData.put("grant_type", "client_credentials");
            tokenData.put("scope", "create:users read:users update:users");
            
            log.info("Solicitando token da Management API para client_id: {}", auth0Config.getClientId());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(tokenData, headers);
            
            String url = String.format("https://%s/oauth/token", auth0Config.getDomain());
            log.info("Fazendo requisição para: {}", url);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            log.info("Resposta da Management API - Status: {}", response.getStatusCode());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode tokenNode = objectMapper.readTree(response.getBody());
                String accessToken = tokenNode.get("access_token").asText();
                log.info("Token obtido com sucesso, tamanho: {}", accessToken.length());
                return accessToken;
            } else {
                log.error("Falha ao obter token - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                throw new RuntimeException("Falha ao obter token da Management API: " + response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Erro ao obter token da Management API: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao obter token da Management API", e);
        }
    }

    /**
     * DTO para representar um usuário Auth0
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Auth0User {
        private String auth0Id;
        private String email;
        private String firstName;
        private String lastName;
        private boolean emailVerified;
    }

    /**
     * DTO para resposta de login Auth0
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Auth0LoginResponse {
        private String accessToken;
        private String tokenType;
        private int expiresIn;
    }
}
