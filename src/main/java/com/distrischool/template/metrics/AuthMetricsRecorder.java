package com.distrischool.template.metrics;

import com.distrischool.template.entity.Role;
import com.distrischool.template.entity.UserRole;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Centraliza o registro de métricas de domínio do microsserviço de autenticação.
 */
@Component
public class AuthMetricsRecorder {

    private static final String METRIC_AUTH_OPERATIONS = "auth_operations_total";
    private static final String METRIC_AUTH_EVENTS = "auth_events_total";
    private static final String METRIC_AUTH_ROLE_ASSIGNMENTS = "auth_role_assignments_total";

    private final MeterRegistry meterRegistry;

    public AuthMetricsRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordOperation(String operation, String outcome) {
        meterRegistry.counter(
            METRIC_AUTH_OPERATIONS,
            "operation", operation,
            "outcome", outcome
        ).increment();
    }

    public void recordAuthEvent(String eventType, String outcome) {
        meterRegistry.counter(
            METRIC_AUTH_EVENTS,
            "event_type", eventType,
            "outcome", outcome
        ).increment();
    }

    public void recordRoleAssignments(Collection<Role> roles, String source) {
        if (roles == null) {
            return;
        }
        roles.stream()
            .map(Role::getName)
            .map(UserRole::name)
            .forEach(roleName -> recordRoleAssignment(roleName, source));
    }

    public void recordRoleAssignment(String roleName, String source) {
        meterRegistry.counter(
            METRIC_AUTH_ROLE_ASSIGNMENTS,
            "role", roleName.toLowerCase(),
            "source", source
        ).increment();
    }
}


