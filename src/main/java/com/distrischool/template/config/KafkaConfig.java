package com.distrischool.template.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuração dos tópicos Kafka.
 * Os tópicos serão criados automaticamente se não existirem.
 */
@Configuration
public class KafkaConfig {

    @Value("${microservice.kafka.topics.example-event}")
    private String exampleEventTopic;

    @Value("${microservice.kafka.topics.example-command}")
    private String exampleCommandTopic;

    @Bean
    public NewTopic exampleEventTopic() {
        return TopicBuilder.name(exampleEventTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic exampleCommandTopic() {
        return TopicBuilder.name(exampleCommandTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

