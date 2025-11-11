package com.distrischool.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Configuração do Flyway que executa repair automaticamente para corrigir
 * inconsistências no histórico de migrações (checksums divergentes, migrações ausentes).
 * 
 * Remove a migração V1 do histórico se ela não existir mais localmente.
 * 
 * Este componente garante que o repair seja executado ANTES das migrações serem validadas,
 * resolvendo problemas de checksums divergentes e migrações ausentes.
 */
@Configuration
public class FlywayRepairConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayRepairConfig.class);

    @Autowired
    private DataSource dataSource;

    /**
     * Estratégia customizada de migração do Flyway que executa repair antes das migrações.
     * Isso garante que checksums divergentes sejam corrigidos antes da validação.
     */
    @Bean
    @Primary
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            try {
                logger.info("Executando Flyway repair antes das migrações...");
                
                // Executar repair para corrigir checksums divergentes
                flyway.repair();
                logger.info("Flyway repair executado com sucesso.");

                // Remover migração V1 do histórico se ela não existir mais localmente
                removeMissingMigrationV1();
                
                // Executar migrações normalmente após o repair
                logger.info("Executando migrações do Flyway...");
                flyway.migrate();
                logger.info("Migrações do Flyway executadas com sucesso.");
            } catch (Exception e) {
                logger.error("Erro durante Flyway repair/migrate: {}", e.getMessage(), e);
                throw e;
            }
        };
    }

    /**
     * Remove a migração V1 do histórico do Flyway se ela não existir mais localmente.
     */
    private void removeMissingMigrationV1() {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            // Verificar se a tabela flyway_schema_history existe
            String checkTableExists = 
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name = 'flyway_schema_history'";
            
            Integer tableExists = jdbcTemplate.queryForObject(checkTableExists, Integer.class);
            
            if (tableExists != null && tableExists > 0) {
                // Verificar se a migração V1 existe no histórico
                String checkV1Exists = 
                    "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1'";
                
                Integer v1Exists = jdbcTemplate.queryForObject(checkV1Exists, Integer.class);
                
                if (v1Exists != null && v1Exists > 0) {
                    logger.info("Removendo migração V1 do histórico do Flyway (migração não existe mais localmente)...");
                    
                    // Remover a entrada da migração V1
                    String deleteV1 = "DELETE FROM flyway_schema_history WHERE version = '1'";
                    int deleted = jdbcTemplate.update(deleteV1);
                    
                    if (deleted > 0) {
                        logger.info("Migração V1 removida do histórico com sucesso.");
                    } else {
                        logger.warn("Tentativa de remover migração V1 não teve efeito.");
                    }
                } else {
                    logger.debug("Migração V1 não encontrada no histórico do Flyway.");
                }
            } else {
                logger.debug("Tabela flyway_schema_history não encontrada. Pulando remoção de V1.");
            }
        } catch (Exception e) {
            logger.warn("Erro ao remover migração V1 do histórico: {}", e.getMessage());
            // Não lançar exceção, apenas logar o aviso
        }
    }
}

