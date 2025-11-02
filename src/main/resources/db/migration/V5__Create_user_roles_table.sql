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
        
        -- Comentários na tabela users
        COMMENT ON TABLE users IS 'Tabela de usuários do sistema DistriSchool';
        COMMENT ON COLUMN users.id IS 'Identificador único do usuário';
        COMMENT ON COLUMN users.email IS 'Email do usuário (único)';
        COMMENT ON COLUMN users.password IS 'Senha criptografada (BCrypt) - opcional se usar apenas Auth0';
        COMMENT ON COLUMN users.first_name IS 'Primeiro nome do usuário';
        COMMENT ON COLUMN users.last_name IS 'Sobrenome do usuário';
        COMMENT ON COLUMN users.phone IS 'Telefone de contato';
        COMMENT ON COLUMN users.document_number IS 'CPF ou documento de identificação';
        COMMENT ON COLUMN users.auth0_id IS 'ID do usuário no Auth0 (OAuth2)';
        COMMENT ON COLUMN users.email_verified IS 'Indica se o email foi verificado';
        COMMENT ON COLUMN users.active IS 'Indica se o usuário está ativo';
        COMMENT ON COLUMN users.last_login IS 'Data e hora do último login';
        COMMENT ON COLUMN users.password_reset_token IS 'Token para recuperação de senha';
        COMMENT ON COLUMN users.password_reset_expires_at IS 'Expiração do token de recuperação';
        COMMENT ON COLUMN users.email_verification_token IS 'Token para verificação de email';
        COMMENT ON COLUMN users.email_verification_expires_at IS 'Expiração do token de verificação';
        COMMENT ON COLUMN users.failed_login_attempts IS 'Contador de tentativas de login falhadas';
        COMMENT ON COLUMN users.locked_until IS 'Data até quando a conta está bloqueada';
        COMMENT ON COLUMN users.created_at IS 'Data de criação do registro';
        COMMENT ON COLUMN users.updated_at IS 'Data da última atualização do registro';
        COMMENT ON COLUMN users.created_by IS 'Usuário que criou o registro';
        COMMENT ON COLUMN users.updated_by IS 'Usuário que atualizou o registro';
        COMMENT ON COLUMN users.deleted_at IS 'Data de exclusão lógica do registro';
        COMMENT ON COLUMN users.deleted_by IS 'Usuário que excluiu o registro';
        
        -- Criar índices
        CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
        CREATE INDEX IF NOT EXISTS idx_user_auth0_id ON users(auth0_id);
        CREATE INDEX IF NOT EXISTS idx_user_document_number ON users(document_number);
        CREATE INDEX IF NOT EXISTS idx_user_active ON users(active);
        CREATE INDEX IF NOT EXISTS idx_user_deleted_at ON users(deleted_at);
    END IF;

    -- Criar tabela roles se não existir (corrige estado inconsistente do Flyway)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_name = 'roles'
    ) THEN
        -- Recriar a tabela roles conforme definição de V3
        CREATE TABLE roles (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(50) NOT NULL UNIQUE,
            description VARCHAR(500),
            active BOOLEAN NOT NULL DEFAULT true,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            created_by VARCHAR(255),
            updated_by VARCHAR(255),
            deleted_at TIMESTAMP,
            deleted_by VARCHAR(255)
        );
        
        -- Comentários na tabela roles
        COMMENT ON TABLE roles IS 'Tabela de perfis de acesso (roles) do sistema DistriSchool';
        COMMENT ON COLUMN roles.id IS 'Identificador único da role';
        COMMENT ON COLUMN roles.name IS 'Nome da role (ADMIN, TEACHER, STUDENT, PARENT)';
        COMMENT ON COLUMN roles.description IS 'Descrição da role';
        COMMENT ON COLUMN roles.active IS 'Indica se a role está ativa';
        COMMENT ON COLUMN roles.created_at IS 'Data de criação do registro';
        COMMENT ON COLUMN roles.updated_at IS 'Data da última atualização do registro';
        COMMENT ON COLUMN roles.created_by IS 'Usuário que criou o registro';
        COMMENT ON COLUMN roles.updated_by IS 'Usuário que atualizou o registro';
        COMMENT ON COLUMN roles.deleted_at IS 'Data de exclusão lógica do registro';
        COMMENT ON COLUMN roles.deleted_by IS 'Usuário que excluiu o registro';
        
        -- Inserção dos 4 perfis de acesso padrão do sistema
        INSERT INTO roles (name, description, active, created_by) VALUES
            ('ADMIN', 'Administrador do Sistema - Acesso total ao sistema', true, 'SYSTEM'),
            ('TEACHER', 'Professor - Acesso para gerenciar turmas, alunos, notas e avaliações', true, 'SYSTEM'),
            ('STUDENT', 'Estudante/Aluno - Acesso para visualizar notas, horários e materiais', true, 'SYSTEM'),
            ('PARENT', 'Pai/Responsável - Acesso para acompanhar o desempenho dos filhos', true, 'SYSTEM')
        ON CONFLICT (name) DO NOTHING;
    END IF;

    -- Criar tabela system_config se não existir (corrige estado inconsistente do Flyway)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_name = 'system_config'
    ) THEN
        -- Recriar a tabela system_config conforme definição de V2
        CREATE TABLE system_config (
            id BIGSERIAL PRIMARY KEY,
            config_key VARCHAR(255) NOT NULL UNIQUE,
            config_value TEXT,
            description TEXT,
            active BOOLEAN DEFAULT true,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            created_by VARCHAR(255),
            updated_by VARCHAR(255)
        );
        
        -- Criar índices
        CREATE INDEX IF NOT EXISTS idx_system_config_key ON system_config(config_key);
        CREATE INDEX IF NOT EXISTS idx_system_config_active ON system_config(active);
        
        -- Comentários na tabela system_config
        COMMENT ON TABLE system_config IS 'Configurações do sistema';
        COMMENT ON COLUMN system_config.config_key IS 'Chave da configuração';
        COMMENT ON COLUMN system_config.config_value IS 'Valor da configuração';
        COMMENT ON COLUMN system_config.description IS 'Descrição da configuração';
        
        -- Criar função para atualizar updated_at se não existir
        CREATE OR REPLACE FUNCTION update_updated_at_column()
        RETURNS TRIGGER AS $function$
        BEGIN
            NEW.updated_at = CURRENT_TIMESTAMP;
            RETURN NEW;
        END;
        $function$ language plpgsql;
        
        -- Criar trigger para system_config
        DROP TRIGGER IF EXISTS update_system_config_updated_at ON system_config;
        CREATE TRIGGER update_system_config_updated_at
            BEFORE UPDATE ON system_config
            FOR EACH ROW
            EXECUTE FUNCTION update_updated_at_column();
        
        -- Inserir configurações padrão
        INSERT INTO system_config (config_key, config_value, description, created_by) VALUES 
            ('app.version', '1.0.0', 'Versão da aplicação', 'system'),
            ('app.environment', 'development', 'Ambiente de execução', 'system'),
            ('kafka.enabled', 'true', 'Habilita integração com Kafka', 'system'),
            ('redis.cache.ttl', '3600', 'TTL padrão do cache Redis em segundos', 'system')
        ON CONFLICT (config_key) DO NOTHING;
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

