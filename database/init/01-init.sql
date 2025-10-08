-- Script de inicialização do banco de dados PostgreSQL
-- Este script é executado automaticamente quando o container PostgreSQL é criado
-- Localização: ./database/init/01-init.sql

-- Criar extensões úteis
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Criar schema adicional se necessário
-- CREATE SCHEMA IF NOT EXISTS distrischool;

-- Configurações de performance para desenvolvimento
-- (Em produção, essas configurações devem ser ajustadas conforme necessário)

-- Configurar timezone padrão
SET timezone = 'UTC';

-- Log de inicialização
DO $$
BEGIN
    RAISE NOTICE 'DistriSchool Template Database initialized successfully!';
    RAISE NOTICE 'Database: %', current_database();
    RAISE NOTICE 'User: %', current_user;
    RAISE NOTICE 'Timezone: %', current_setting('timezone');
END $$;
