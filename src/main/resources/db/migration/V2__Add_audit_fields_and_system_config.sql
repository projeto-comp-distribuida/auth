-- Migração para adicionar campos de auditoria
-- Criada em: 2024-01-02
-- Descrição: Adiciona campos de auditoria e soft delete

-- Adicionar campos de auditoria
ALTER TABLE example_entity 
ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE,
ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- Comentários nos novos campos
COMMENT ON COLUMN example_entity.created_by IS 'Usuário que criou o registro';
COMMENT ON COLUMN example_entity.updated_by IS 'Usuário que fez a última atualização';
COMMENT ON COLUMN example_entity.deleted_at IS 'Data e hora da exclusão lógica';
COMMENT ON COLUMN example_entity.deleted_by IS 'Usuário que fez a exclusão lógica';

-- Criar função para atualizar automaticamente o campo updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Criar trigger para atualizar updated_at automaticamente
DROP TRIGGER IF EXISTS update_example_entity_updated_at ON example_entity;
CREATE TRIGGER update_example_entity_updated_at
    BEFORE UPDATE ON example_entity
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Criar tabela de configurações do sistema
CREATE TABLE IF NOT EXISTS system_config (
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

-- Índices para system_config
CREATE INDEX IF NOT EXISTS idx_system_config_key ON system_config(config_key);
CREATE INDEX IF NOT EXISTS idx_system_config_active ON system_config(active);

-- Comentários na tabela system_config
COMMENT ON TABLE system_config IS 'Configurações do sistema';
COMMENT ON COLUMN system_config.config_key IS 'Chave da configuração';
COMMENT ON COLUMN system_config.config_value IS 'Valor da configuração';
COMMENT ON COLUMN system_config.description IS 'Descrição da configuração';

-- Trigger para system_config
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
