package com.distrischool.auth.repository;

import com.distrischool.auth.entity.User;
import com.distrischool.auth.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para operações de banco de dados relacionadas à entidade User.
 * Gerencia os usuários do sistema DistriSchool.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca um usuário pelo email
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca um usuário pelo email ignorando caso
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Busca um usuário pelo Auth0 ID
     */
    Optional<User> findByAuth0Id(String auth0Id);

    /**
     * Busca um usuário pelo número de documento (CPF)
     */
    Optional<User> findByDocumentNumber(String documentNumber);


    /**
     * Verifica se existe um usuário com o email especificado
     */
    boolean existsByEmail(String email);

    /**
     * Verifica se existe um usuário com o email especificado (ignorando caso)
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Verifica se existe um usuário com o Auth0 ID especificado
     */
    boolean existsByAuth0Id(String auth0Id);

    /**
     * Verifica se existe um usuário com o número de documento especificado
     */
    boolean existsByDocumentNumber(String documentNumber);

    /**
     * Busca usuários ativos
     */
    @Query("SELECT u FROM User u WHERE u.active = true AND u.deletedAt IS NULL")
    List<User> findAllActiveUsers();

    /**
     * Busca usuários por role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role AND u.active = true AND u.deletedAt IS NULL")
    List<User> findByRole(@Param("role") UserRole role);


    /**
     * Busca usuários por nome (primeiro ou último nome)
     */
    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) AND u.deletedAt IS NULL")
    List<User> findByNameContaining(@Param("name") String name);

    /**
     * Busca usuários que fizeram login após uma data específica
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :since AND u.deletedAt IS NULL")
    List<User> findUsersLoggedInSince(@Param("since") LocalDateTime since);

    /**
     * Conta usuários ativos por role
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :role AND u.active = true AND u.deletedAt IS NULL")
    Long countActiveUsersByRole(@Param("role") UserRole role);

    /**
     * Busca usuário para autenticação (local auth only - Auth0 handles verification)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true AND u.deletedAt IS NULL")
    Optional<User> findForAuthentication(@Param("email") String email);
}

