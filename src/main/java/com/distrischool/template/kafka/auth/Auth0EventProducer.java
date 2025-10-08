package com.distrischool.template.kafka.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Producer Kafka para eventos de autenticação via Auth0.
 * Publica eventos relacionados ao microsserviço de autenticação.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Auth0EventProducer {

    private final KafkaTemplate<String, AuthEvent> kafkaTemplate;
    
    // Tópicos específicos para eventos de autenticação
    private static final String USER_CREATED_TOPIC = "distrischool.auth.user.created";
    private static final String USER_LOGGED_TOPIC = "distrischool.auth.user.logged";
    private static final String USER_UPDATED_TOPIC = "distrischool.auth.user.updated";
    private static final String USER_DEACTIVATED_TOPIC = "distrischool.auth.user.deactivated";

    /**
     * Publica evento de usuário criado via Auth0
     */
    public void publishUserCreated(Long userId, String email, String auth0Id, String firstName, String lastName) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("email", email);
        data.put("auth0Id", auth0Id);
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        
        UserCreatedEvent event = new UserCreatedEvent(data);
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("USER_CREATED");
        event.setTimestamp(LocalDateTime.now());
        event.setSource("AUTH0");
        
        sendEvent(USER_CREATED_TOPIC, event);
    }

    /**
     * Publica evento de usuário logado via Auth0
     */
    public void publishUserLogged(Long userId, String email, String auth0Id) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("email", email);
        data.put("auth0Id", auth0Id);
        
        UserLoggedEvent event = new UserLoggedEvent(data);
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("USER_LOGGED");
        event.setTimestamp(LocalDateTime.now());
        event.setSource("AUTH0");
        
        sendEvent(USER_LOGGED_TOPIC, event);
    }

    /**
     * Publica evento de usuário atualizado via Auth0
     */
    public void publishUserUpdated(Long userId, String email, String auth0Id, String firstName, String lastName) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("email", email);
        data.put("auth0Id", auth0Id);
        data.put("firstName", firstName);
        data.put("lastName", lastName);
        
        UserUpdatedEvent event = new UserUpdatedEvent(data);
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("USER_UPDATED");
        event.setTimestamp(LocalDateTime.now());
        event.setSource("AUTH0");
        
        sendEvent(USER_UPDATED_TOPIC, event);
    }

    /**
     * Publica evento de usuário desativado via Auth0
     */
    public void publishUserDeactivated(Long userId, String email, String auth0Id, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId.toString());
        data.put("email", email);
        data.put("auth0Id", auth0Id);
        data.put("reason", reason);
        
        UserDeactivatedEvent event = new UserDeactivatedEvent(data);
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType("USER_DEACTIVATED");
        event.setTimestamp(LocalDateTime.now());
        event.setSource("AUTH0");
        
        sendEvent(USER_DEACTIVATED_TOPIC, event);
    }

    /**
     * Envia um evento de autenticação para um tópico específico
     */
    private void sendEvent(String topic, AuthEvent event) {
        log.info("Enviando evento de autenticação Auth0 para tópico '{}': {}", topic, event.getEventType());
        
        CompletableFuture<SendResult<String, AuthEvent>> future = 
            kafkaTemplate.send(topic, event.getEventId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Evento de autenticação Auth0 enviado com sucesso. Tópico: {}, Partition: {}, Offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Erro ao enviar evento de autenticação Auth0 para o tópico '{}': {}", 
                        topic, ex.getMessage(), ex);
            }
        });
    }
}
