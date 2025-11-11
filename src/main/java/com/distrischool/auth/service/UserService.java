package com.distrischool.auth.service;

import com.distrischool.auth.entity.Role;
import com.distrischool.auth.entity.User;
import com.distrischool.auth.entity.UserRole;
import com.distrischool.auth.exception.BusinessException;
import com.distrischool.auth.exception.ResourceNotFoundException;
import com.distrischool.auth.kafka.auth.Auth0EventProducer;
import com.distrischool.auth.repository.RoleRepository;
import com.distrischool.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço para gerenciamento de usuários.
 * Implementa operações CRUD e lógicas de negócio relacionadas a usuários.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Auth0EventProducer auth0EventProducer;


    /**
     * Busca usuário por ID
     */
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
    }

    /**
     * Busca usuário por email
     */
    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com email: " + email));
    }

    /**
     * Busca usuário por Auth0 ID
     */
    public User findByAuth0Id(String auth0Id) {
        return userRepository.findByAuth0Id(auth0Id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com Auth0 ID: " + auth0Id));
    }

    /**
     * Lista todos os usuários ativos
     */
    public List<User> findAllActiveUsers() {
        return userRepository.findAllActiveUsers();
    }

    /**
     * Busca usuários por role
     */
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Email verification, password reset, and login attempt tracking
     * are handled by Auth0 in production.
     * 
     * Removed methods:
     * - verifyEmail() -> Use Auth0 email verification
     * - initiatePasswordReset() -> Use Auth0 password reset flow
     * - resetPassword() -> Use Auth0 password reset flow
     * - incrementFailedLoginAttempts() -> Auth0 handles this with Anomaly Detection
     */

    /**
     * Atualiza último login do usuário
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        User user = findById(userId);
        user.updateLastLogin();
        userRepository.save(user);
        
        // Publica evento de login
        auth0EventProducer.publishUserLogged(userId, user.getEmail(), user.getAuth0Id());
    }

    /**
     * Cria ou atualiza usuário a partir de um login Auth0
     * 
     * Sincroniza dados do Auth0 com o banco local.
     * Chamado automaticamente após autenticação bem-sucedida via Auth0.
     */
    @Transactional
    public User syncAuth0User(String auth0Id, String email, String firstName, String lastName) {
        // Busca usuário existente por Auth0 ID
        return userRepository.findByAuth0Id(auth0Id)
            .map(user -> {
                // Atualiza dados se mudaram
                boolean updated = false;
                if (!user.getEmail().equals(email)) {
                    user.setEmail(email);
                    updated = true;
                }
                if (firstName != null && !firstName.equals(user.getFirstName())) {
                    user.setFirstName(firstName);
                    updated = true;
                }
                if (lastName != null && !lastName.equals(user.getLastName())) {
                    user.setLastName(lastName);
                    updated = true;
                }
                
                if (updated) {
                    user.setUpdatedBy("AUTH0_SYNC");
                    userRepository.save(user);
                    log.info("Usuário Auth0 atualizado: {}", email);
                    
                    // Publica evento de usuário atualizado
                    auth0EventProducer.publishUserUpdated(user.getId(), email, auth0Id, firstName, lastName);
                }
                
                return user;
            })
            .orElseGet(() -> {
                // Cria novo usuário
                User newUser = User.builder()
                    .auth0Id(auth0Id)
                    .email(email.toLowerCase().trim())
                    .firstName(firstName != null ? firstName : "")
                    .lastName(lastName != null ? lastName : "")
                    .active(true)
                    .build();
                
                // Adiciona role padrão (STUDENT por exemplo, ou baseado em regras)
                roleRepository.findByName(UserRole.STUDENT)
                    .ifPresent(newUser::addRole);
                
                newUser.setCreatedBy("AUTH0_SYNC");
                newUser.setUpdatedBy("AUTH0_SYNC");
                
                User saved = userRepository.save(newUser);
                log.info("Novo usuário criado a partir do Auth0: {}", email);
                
                // Publica evento de usuário criado
                auth0EventProducer.publishUserCreated(saved.getId(), email, auth0Id, firstName, lastName);
                
                return saved;
            });
    }

    /**
     * Vincula usuário local existente ao Auth0
     */
    @Transactional
    public void linkAuth0Account(Long userId, String auth0Id) {
        User user = findById(userId);
        
        if (userRepository.existsByAuth0Id(auth0Id)) {
            throw new BusinessException("Auth0 ID já vinculado a outro usuário");
        }

        user.setAuth0Id(auth0Id);
        user.setUpdatedBy("SYSTEM");
        userRepository.save(user);
        
        log.info("Usuário {} vinculado ao Auth0 com ID: {}", user.getEmail(), auth0Id);
    }

    /**
     * Desativa usuário (soft delete)
     */
    @Transactional
    public void deactivateUser(Long userId, String deletedBy) {
        User user = findById(userId);
        user.setActive(false);
        user.markAsDeleted(deletedBy);
        userRepository.save(user);
        log.info("Usuário desativado: {}", user.getEmail());
        
        // Publica evento de usuário desativado
        auth0EventProducer.publishUserDeactivated(userId, user.getEmail(), user.getAuth0Id(), "Deactivated by: " + deletedBy);
    }

    /**
     * Reativa usuário
     */
    @Transactional
    public void reactivateUser(Long userId) {
        User user = findById(userId);
        user.setActive(true);
        user.restore();
        user.setUpdatedBy("SYSTEM");
        userRepository.save(user);
        log.info("Usuário reativado: {}", user.getEmail());
    }
}

