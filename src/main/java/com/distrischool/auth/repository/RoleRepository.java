package com.distrischool.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.distrischool.auth.entity.Role;
import com.distrischool.auth.entity.UserRole;

/**
 * Repository para operações de banco de dados relacionadas à entidade Role.
 * Gerencia os perfis de acesso do DistriSchool.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Busca uma role pelo nome (enum UserRole)
     */
    Optional<Role> findByName(UserRole name);

    /**
     * Busca roles ativas
     */
    List<Role> findByActiveTrue();

    /**
     * Busca roles ativas que não foram excluídas logicamente
     */
    @Query("SELECT r FROM Role r WHERE r.active = true AND r.deletedAt IS NULL")
    List<Role> findAllActiveRoles();

    /**
     * Verifica se existe uma role com o nome especificado
     */
    boolean existsByName(UserRole name);

    /**
     * Busca roles por descrição (busca parcial, case-insensitive)
     */
    @Query("SELECT r FROM Role r WHERE LOWER(r.description) LIKE LOWER(CONCAT('%', :description, '%')) AND r.deletedAt IS NULL")
    List<Role> findByDescriptionContainingIgnoreCase(String description);
}

