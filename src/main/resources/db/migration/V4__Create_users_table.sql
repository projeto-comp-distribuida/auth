-- =====================================================
-- DistriSchool - Sistema de Gestão Escolar Distribuído
-- Migration: Criação da tabela de Users (Usuários)
-- Versão: V4
-- Data: 2025-10-08
-- =====================================================

-- Criação da tabela de usuários
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

-- Comentários na tabela
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

-- Índices para otimizar consultas
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_auth0_id ON users(auth0_id);
CREATE INDEX idx_user_document_number ON users(document_number);
CREATE INDEX idx_user_active ON users(active);
CREATE INDEX idx_user_deleted_at ON users(deleted_at);

