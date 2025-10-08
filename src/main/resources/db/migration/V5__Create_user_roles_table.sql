-- =====================================================
-- DistriSchool - Sistema de Gestão Escolar Distribuído
-- Migration: Criação da tabela de User_Roles (Relação N:N)
-- Versão: V5
-- Data: 2025-10-08
-- =====================================================

-- Criação da tabela de relacionamento entre usuários e roles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Comentários na tabela
COMMENT ON TABLE user_roles IS 'Tabela de relacionamento entre usuários e roles (N:N)';
COMMENT ON COLUMN user_roles.user_id IS 'Referência ao ID do usuário';
COMMENT ON COLUMN user_roles.role_id IS 'Referência ao ID da role';

-- Índices para otimizar consultas
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Criação de um usuário administrador padrão para facilitar testes
-- Senha: Admin@123 (criptografada com BCrypt)
-- IMPORTANTE: Alterar em produção!
INSERT INTO users (email, password, first_name, last_name, email_verified, active, created_by)
VALUES (
    'admin@distrischool.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYCj.Gcg9KK',
    'Admin',
    'DistriSchool',
    true,
    true,
    'SYSTEM'
);

-- Associa o usuário administrador à role ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@distrischool.com' AND r.name = 'ADMIN';

