-- =====================================================
-- DistriSchool - Sistema de Gestão Escolar Distribuído
-- Migration: Criação da tabela de Roles (Perfis de Acesso)
-- Versão: V3
-- Data: 2025-10-08
-- =====================================================

-- Criação da tabela de roles
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

-- Comentários na tabela
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
    ('PARENT', 'Pai/Responsável - Acesso para acompanhar o desempenho dos filhos', true, 'SYSTEM');

