package com.distrischool.auth.entity;

/**
 * Enum para os tipos de usuários do DistriSchool.
 * Define os 4 perfis de acesso ao sistema de gestão escolar.
 */
public enum UserRole {
    ADMIN("Administrador do Sistema"),
    TEACHER("Professor"),
    STUDENT("Estudante/Aluno"),
    PARENT("Pai/Responsável");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Retorna o nome da role formatado para Spring Security (com prefixo ROLE_)
     */
    public String getRoleName() {
        return "ROLE_" + this.name();
    }
}

