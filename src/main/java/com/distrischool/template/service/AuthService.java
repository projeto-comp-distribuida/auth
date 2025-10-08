package com.distrischool.template.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.distrischool.template.dto.auth.AuthResponse;
import com.distrischool.template.dto.auth.LoginRequest;
import com.distrischool.template.dto.auth.RegisterRequest;
import com.distrischool.template.entity.Role;
import com.distrischool.template.entity.User;
import com.distrischool.template.exception.BusinessException;
import com.distrischool.template.kafka.auth.Auth0EventProducer;
import com.distrischool.template.repository.RoleRepository;
import com.distrischool.template.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço de autenticação com Auth0.
 * Gerencia registro e sincronização de usuários.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Auth0ManagementService auth0ManagementService;
    private final Auth0EventProducer auth0EventProducer;

    /**
     * Realiza login de um usuário via Auth0
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Tentativa de login para email: {}", request.getEmail());

        try {
            // 1. Autenticar com Auth0 e obter token
            Auth0ManagementService.Auth0LoginResponse auth0Response = 
                auth0ManagementService.login(request.getEmail(), request.getPassword());
            
            // 2. Decodificar token para obter informações do usuário
            DecodedJWT decodedJWT = JWT.decode(auth0Response.getAccessToken());
            String auth0Id = decodedJWT.getSubject();
            String email = decodedJWT.getClaim("email").asString();
            
            // 3. Buscar ou sincronizar usuário no banco local
            User user = userRepository.findByAuth0Id(auth0Id)
                .orElseGet(() -> {
                    // Se não existe, busca por email (pode ser usuário antigo)
                    return userRepository.findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new BusinessException("Usuário não encontrado no sistema"));
                });
            
            // 4. Atualizar último login
            user.updateLastLogin();
            userRepository.save(user);
            
            // 5. Publicar evento de login
            auth0EventProducer.publishUserLogged(user.getId(), user.getEmail(), user.getAuth0Id());
            
            // 6. Construir resposta com token
            return buildAuthResponseWithToken(user, auth0Response.getAccessToken());

        } catch (Exception e) {
            log.error("Erro ao fazer login: {}", e.getMessage());
            throw new BusinessException("Falha na autenticação: " + e.getMessage());
        }
    }

    /**
     * Registra um novo usuário via Auth0
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Tentativa de registro para email: {}", request.getEmail());

        // Valida se as senhas coincidem
        if (!request.isPasswordMatching()) {
            throw new BusinessException("As senhas não coincidem");
        }

        // Valida se o email já existe no banco local
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new BusinessException("Email já cadastrado no sistema");
        }

        // Valida se o CPF já existe (se informado)
        if (request.getDocumentNumber() != null && 
            userRepository.existsByDocumentNumber(request.getDocumentNumber())) {
            throw new BusinessException("CPF já cadastrado no sistema");
        }

        try {
            User savedUser = registerWithAuth0(request);

            // Publicar evento de usuário criado
            publishUserCreatedEvent(savedUser);

            // Construir resposta
            return buildAuthResponse(savedUser);

        } catch (Exception e) {
            log.error("Erro ao registrar usuário: {}", e.getMessage(), e);
            throw new BusinessException("Falha ao registrar usuário: " + e.getMessage());
        }
    }
    
    /**
     * Registra usuário com Auth0
     */
    private User registerWithAuth0(RegisterRequest request) {
        // 1. Criar usuário no Auth0
        Auth0ManagementService.Auth0User auth0User = auth0ManagementService.createUser(
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            request.getPhone(),
            request.getDocumentNumber(),
            request.getRoles().iterator().next() // Pega a primeira role
        );

        // 2. Buscar roles do banco local
        Set<Role> roles = request.getRoles().stream()
            .map(roleName -> roleRepository.findByName(roleName)
                .orElseThrow(() -> new BusinessException("Role não encontrada: " + roleName)))
            .collect(Collectors.toSet());

        // 3. Criar usuário no banco local
        User user = User.builder()
            .email(auth0User.getEmail().toLowerCase().trim())
            .firstName(auth0User.getFirstName().trim())
            .lastName(auth0User.getLastName().trim())
            .phone(request.getPhone())
            .documentNumber(request.getDocumentNumber())
            .auth0Id(auth0User.getAuth0Id())
            .active(true)
            .build();

        // Adicionar roles
        roles.forEach(user::addRole);

        user.setCreatedBy("AUTH0_REGISTRATION");
        user.setUpdatedBy("AUTH0_REGISTRATION");

        User savedUser = userRepository.save(user);
        log.info("Usuário registrado com Auth0 com sucesso: {}", savedUser.getEmail());
        
        return savedUser;
    }
    
    /**
     * Publica evento de usuário criado via Auth0
     */
    private void publishUserCreatedEvent(User user) {
        auth0EventProducer.publishUserCreated(
            user.getId(),
            user.getEmail(),
            user.getAuth0Id(),
            user.getFirstName(),
            user.getLastName()
        );
    }

    /**
     * Constrói resposta de autenticação
     */
    private AuthResponse buildAuthResponse(User user) {
        AuthResponse.UserResponse userResponse = AuthResponse.UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .documentNumber(user.getDocumentNumber())
            .auth0Id(user.getAuth0Id())
            .active(user.getActive())
            .lastLogin(user.getLastLogin())
            .roles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()))
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();

        return AuthResponse.builder()
            .user(userResponse)
            .build();
    }

    /**
     * Constrói resposta de autenticação com token
     */
    private AuthResponse buildAuthResponseWithToken(User user, String token) {
        AuthResponse.UserResponse userResponse = AuthResponse.UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phone(user.getPhone())
            .documentNumber(user.getDocumentNumber())
            .auth0Id(user.getAuth0Id())
            .active(user.getActive())
            .lastLogin(user.getLastLogin())
            .roles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()))
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();

        return AuthResponse.builder()
            .token(token)
            .user(userResponse)
            .build();
    }
}

