-- =====================================================
-- DistriSchool - Sistema de Gestão Escolar Distribuído
-- Migration: Remove campos de autenticação local (Auth0 only)
-- Versão: V6
-- Data: 2025-01-27
-- =====================================================

-- Remove campos relacionados à autenticação local
-- Auth0 agora gerencia completamente: passwords, email verification, password reset, MFA, login attempts

ALTER TABLE users 
DROP COLUMN IF EXISTS password,
DROP COLUMN IF EXISTS email_verified,
DROP COLUMN IF EXISTS password_reset_token,
DROP COLUMN IF EXISTS password_reset_expires_at,
DROP COLUMN IF EXISTS email_verification_token,
DROP COLUMN IF EXISTS email_verification_expires_at,
DROP COLUMN IF EXISTS failed_login_attempts,
DROP COLUMN IF EXISTS locked_until;

-- Comentários atualizados
COMMENT ON TABLE users IS 'Tabela de usuários do sistema DistriSchool - Auth0 authentication only';
COMMENT ON COLUMN users.auth0_id IS 'ID do usuário no Auth0 (OAuth2) - obrigatório para autenticação';

