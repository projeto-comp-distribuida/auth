-- Migração inicial para criar a tabela example_entity
-- Criada em: 2024-01-01
-- Descrição: Cria a tabela de exemplo para demonstrar a estrutura base

-- Criar tabela example_entity
CREATE TABLE example_entity (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Índices para example_entity
CREATE INDEX idx_example_entity_name ON example_entity(name);
CREATE INDEX idx_example_entity_active ON example_entity(active);

-- Comentários na tabela example_entity
COMMENT ON TABLE example_entity IS 'Tabela de exemplo para demonstrar a estrutura base do DistriSchool';
COMMENT ON COLUMN example_entity.name IS 'Nome do exemplo';
COMMENT ON COLUMN example_entity.description IS 'Descrição do exemplo';
COMMENT ON COLUMN example_entity.active IS 'Indica se o exemplo está ativo';
COMMENT ON COLUMN example_entity.created_at IS 'Data de criação do registro';
COMMENT ON COLUMN example_entity.updated_at IS 'Data da última atualização';

-- Inserir dados de exemplo
INSERT INTO example_entity (name, description, active) VALUES 
    ('Exemplo 1', 'Primeiro exemplo de entidade', true),
    ('Exemplo 2', 'Segundo exemplo de entidade', true),
    ('Exemplo 3', 'Terceiro exemplo de entidade', false);
