package com.distrischool.template.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer Kafka para receber eventos de outros microsserviços do DistriSchool.
 * Use este padrão para processar mensagens recebidas.
 */
@Slf4j
@Component
public class EventConsumer {

    /**
     * Consumer genérico para eventos do DistriSchool.
     * Configure os tópicos específicos no application.yml
     */
    @KafkaListener(
            topics = "#{@environment.getProperty('microservice.kafka.topics', 'distrischool.events')}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEvent(
            @Payload DistriSchoolEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        log.info("Evento recebido do tópico '{}' [partition: {}, offset: {}]", topic, partition, offset);
        log.debug("Detalhes do evento: eventId={}, eventType={}, source={}", 
                event.getEventId(), event.getEventType(), event.getSource());
        
        try {
            // Processar o evento aqui
            processEvent(event);
            log.info("Evento processado com sucesso: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Erro ao processar evento {}: {}", event.getEventId(), e.getMessage(), e);
            // Implemente lógica de retry ou DLQ (Dead Letter Queue) aqui se necessário
        }
    }

    /**
     * Processa eventos baseado no tipo
     */
    private void processEvent(DistriSchoolEvent event) {
        log.debug("Processando evento do tipo: {}", event.getEventType());
        
        // Exemplos de eventos do DistriSchool baseados nos requisitos
        switch (event.getEventType()) {
            case "student.created":
                log.info("Processando criação de aluno: {}", event.getData());
                // Implemente lógica específica aqui
                break;
            case "teacher.assigned":
                log.info("Processando atribuição de professor: {}", event.getData());
                // Implemente lógica específica aqui
                break;
            case "schedule.updated":
                log.info("Processando atualização de horário: {}", event.getData());
                // Implemente lógica específica aqui
                break;
            case "attendance.recorded":
                log.info("Processando registro de presença: {}", event.getData());
                // Implemente lógica específica aqui
                break;
            case "user.logged":
                log.info("Processando login de usuário: {}", event.getData());
                // Implemente lógica específica aqui
                break;
            default:
                log.warn("Tipo de evento desconhecido: {}", event.getEventType());
        }
    }

    /**
     * Exemplo de consumer para tópico de comandos.
     * Descomente e ajuste conforme necessário.
     */
    /*
    @KafkaListener(
            topics = "${microservice.kafka.topics.commands}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeCommand(@Payload DistriSchoolEvent command) {
        log.info("Comando recebido: {}", command.getEventType());
        // Processar comando
    }
    */
}