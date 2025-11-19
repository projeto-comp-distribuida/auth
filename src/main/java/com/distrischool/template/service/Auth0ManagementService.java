package com.distrischool.template.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.distrischool.template.config.Auth0Config;
import com.distrischool.template.entity.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
            userData.put("user_metadata", userMetadata);
            
            // Adicionar app_metadata com role (apenas aplicação pode modificar)
            Map<String, Object> appMetadata = new HashMap<>();
            appMetadata.put("role", role.name());
            userData.put("app_metadata", appMetadata);
            
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
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                log.warn("Usuário já existe no Auth0: {}. Tentando buscar usuário existente.", email);
                Auth0User existingUser = findUserByEmail(email);
                if (existingUser != null) {
                    return existingUser;
                }
            }
            log.error("Erro ao criar usuário no Auth0: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao criar usuário no Auth0", e);
        } catch (Exception e) {
            log.error("Erro ao criar usuário no Auth0: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao criar usuário no Auth0", e);
        }
    }

    /**
     * Busca um usuário existente no Auth0 pelo email
     */
    public Auth0User findUserByEmail(String email) {
        try {
            String accessToken = getManagementApiToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> request = new HttpEntity<>(headers);

            String url = String.format("https://%s/api/v2/users-by-email?email=%s",
                    auth0Config.getDomain(),
                    URLEncoder.encode(email, StandardCharsets.UTF_8));

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode arrayNode = objectMapper.readTree(response.getBody());
                if (arrayNode.isArray() && arrayNode.size() > 0) {
                    JsonNode userNode = arrayNode.get(0);
                    log.info("Usuário existente localizado no Auth0 para {}: {}", email, userNode.get("user_id").asText());

                    String givenName = userNode.hasNonNull("given_name") ? userNode.get("given_name").asText() : "";
                    String familyName = userNode.hasNonNull("family_name") ? userNode.get("family_name").asText() : "";

                    return Auth0User.builder()
                            .auth0Id(userNode.get("user_id").asText())
                            .email(userNode.get("email").asText())
                            .firstName(givenName)
                            .lastName(familyName)
                            .emailVerified(userNode.get("email_verified").asBoolean())
                            .build();
                }
            }

            log.warn("Não foi possível localizar usuário existente no Auth0 para {}", email);
            return null;
        } catch (Exception e) {
            log.error("Erro ao buscar usuário no Auth0 por email {}: {}", email, e.getMessage(), e);
            return null;
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
     * Solicita envio de email de redefinição de senha para um usuário
     */
    public void sendPasswordResetEmail(String email) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("client_id", auth0Config.getClientId());
            payload.put("email", email);
            payload.put("connection", auth0Config.getConnection());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            String url = String.format("https://%s/dbconnections/change_password", auth0Config.getDomain());
            log.info("Solicitando reset de senha no Auth0 para {}", email);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Reset de senha solicitado com sucesso para {}. Resposta: {}", email, response.getBody());
                return;
            }

            log.error("Falha ao solicitar reset de senha - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            throw new RuntimeException("Falha ao solicitar reset de senha: " + response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST || e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Auth0 não encontrou usuário para reset de senha: {} ({})", email, e.getResponseBodyAsString());
                return; // Evita revelar se o email existe ou não
            }
            log.error("Erro ao solicitar reset de senha no Auth0: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao solicitar reset de senha", e);
        } catch (Exception e) {
            log.error("Erro inesperado ao solicitar reset de senha: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao solicitar reset de senha", e);
        }
    }

    /**
     * Reseta a senha de um usuário usando token de reset
     * O token deve ser um JWT que contém informações do usuário (email ou auth0Id)
     */
    public void resetPassword(String token, String newPassword) {
        try {
            // Tentar decodificar o token para extrair informações do usuário
            String auth0Id = null;
            String email = null;
            
            try {
                DecodedJWT decodedJWT = JWT.decode(token);
                auth0Id = decodedJWT.getSubject();
                email = decodedJWT.getClaim("email") != null ? decodedJWT.getClaim("email").asString() : null;
                log.info("Token decodificado - auth0Id: {}, email: {}", auth0Id, email);
            } catch (Exception e) {
                log.warn("Não foi possível decodificar token como JWT: {}", e.getMessage());
                // Se não for JWT, tentar usar o token como auth0Id diretamente
                auth0Id = token;
            }
            
            // Se não temos auth0Id, tentar buscar por email
            if (auth0Id == null && email != null) {
                Auth0User user = findUserByEmail(email);
                if (user != null) {
                    auth0Id = user.getAuth0Id();
                }
            }
            
            if (auth0Id == null) {
                throw new RuntimeException("Não foi possível identificar o usuário a partir do token");
            }
            
            // Obter token de acesso para Management API
            String accessToken = getManagementApiToken();
            
            // Preparar dados para atualização
            Map<String, String> updateData = new HashMap<>();
            updateData.put("password", newPassword);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            HttpEntity<Map<String, String>> request = new HttpEntity<>(updateData, headers);
            
            // URL para atualizar usuário específico
            String url = String.format("https://%s/api/v2/users/%s", auth0Config.getDomain(), 
                    URLEncoder.encode(auth0Id, StandardCharsets.UTF_8));
            log.info("Resetando senha no Auth0 para usuário: {}", auth0Id);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Senha resetada com sucesso para usuário: {}", auth0Id);
                return;
            }
            
            log.error("Falha ao resetar senha - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            throw new RuntimeException("Falha ao resetar senha: " + response.getBody());
            
        } catch (HttpClientErrorException e) {
            log.error("Erro ao resetar senha no Auth0: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao resetar senha: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao resetar senha: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao resetar senha", e);
        }
    }

    /**
     * Verifica o email de um usuário usando token de verificação
     * O token deve ser um JWT que contém informações do usuário (email ou auth0Id)
     */
    public void verifyEmail(String token) {
        try {
            // Tentar decodificar o token para extrair informações do usuário
            String auth0Id = null;
            String email = null;
            
            try {
                DecodedJWT decodedJWT = JWT.decode(token);
                auth0Id = decodedJWT.getSubject();
                email = decodedJWT.getClaim("email") != null ? decodedJWT.getClaim("email").asString() : null;
                log.info("Token decodificado - auth0Id: {}, email: {}", auth0Id, email);
            } catch (Exception e) {
                log.warn("Não foi possível decodificar token como JWT: {}", e.getMessage());
                // Se não for JWT, tentar usar o token como auth0Id diretamente
                auth0Id = token;
            }
            
            // Se não temos auth0Id, tentar buscar por email
            if (auth0Id == null && email != null) {
                Auth0User user = findUserByEmail(email);
                if (user != null) {
                    auth0Id = user.getAuth0Id();
                }
            }
            
            if (auth0Id == null) {
                throw new RuntimeException("Não foi possível identificar o usuário a partir do token");
            }
            
            // Obter token de acesso para Management API
            String accessToken = getManagementApiToken();
            
            // Preparar dados para atualização
            Map<String, Boolean> updateData = new HashMap<>();
            updateData.put("email_verified", true);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            HttpEntity<Map<String, Boolean>> request = new HttpEntity<>(updateData, headers);
            
            // URL para atualizar usuário específico
            String url = String.format("https://%s/api/v2/users/%s", auth0Config.getDomain(), 
                    URLEncoder.encode(auth0Id, StandardCharsets.UTF_8));
            log.info("Verificando email no Auth0 para usuário: {}", auth0Id);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PATCH, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email verificado com sucesso para usuário: {}", auth0Id);
                return;
            }
            
            log.error("Falha ao verificar email - Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            throw new RuntimeException("Falha ao verificar email: " + response.getBody());
            
        } catch (HttpClientErrorException e) {
            log.error("Erro ao verificar email no Auth0: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao verificar email: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao verificar email: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao verificar email", e);
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
