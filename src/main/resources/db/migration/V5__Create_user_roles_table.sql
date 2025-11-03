-- =====================================================
-- DistriSchool - Sistema de Gestão Escolar Distribuído
-- Migration: Criação da tabela de User_Roles (Relação N:N)
-- Versão: V5
-- Data: 2025-10-08
-- =====================================================

-- Garantir que as tabelas de dependência existam (corrige estado inconsistente)
-- Se V4 foi marcada como executada mas a tabela não foi criada, criamos aqui
DO $$
BEGIN
    -- Criar tabela users se não existir (corrige estado inconsistente do Flyway)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_name = 'users'
    ) THEN
        -- Recriar a tabela users conforme definição de V4
        CREATE TABLE users (
            id BIGSERIAL PRIMARY KEY,
            email VARCHAR(255) NOT NULL UNIQUE,
            password VARCHAR(255),
            first_name VARCHAR(100) NOT NULL,
            last_name VARCHAR(100) NOT NULL,
            phone VARCHAR(20),
            document_number VARCHAR(50) UNIQUE,
            auth0_id VARCHAR(255) UNIQUE,
            email_verified BOOLEAN NOT NULL DEFAULT false,
            active BOOLEAN NOT NULL DEFAULT true,
            last_login TIMESTAMP,
            password_reset_token VARCHAR(500),
            password_reset_expires_at TIMESTAMP,
            email_verification_token VARCHAR(500),
            email_verification_expires_at TIMESTAMP,
            failed_login_attempts INTEGER NOT NULL DEFAULT 0,
            locked_until TIMESTAMP,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            created_by VARCHAR(255),
            updated_by VARCHAR(255),
            deleted_at TIMESTAMP,
            deleted_by VARCHAR(255)
        );
        
        -- Criar índices
        CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
        CREATE INDEX IF NOT EXISTS idx_user_auth0_id ON users(auth0_id);
        CREATE INDEX IF NOT EXISTS idx_user_document_number ON users(document_number);
        CREATE INDEX IF NOT EXISTS idx_user_active ON users(active);
        CREATE INDEX IF NOT EXISTS idx_user_deleted_at ON users(deleted_at);
    END IF;

    -- Verificar se a tabela roles existe
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_name = 'roles'
    ) THEN
        RAISE EXCEPTION 'Tabela roles não existe. Execute a migração V3 primeiro.';
    END IF;
END $$;

-- Criação da tabela de relacionamento entre usuários e roles
CREATE TABLE IF NOT EXISTS user_roles (
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

-- Índices para otimizar consultas (idempotentes)
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

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
)
ON CONFLICT (email) DO NOTHING;

-- Associa o usuário administrador à role ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@distrischool.com' AND r.name = 'ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;

