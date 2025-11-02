package com.distrischool.template.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para criar usuário internamente (chamado por outros serviços)
 * Não requer senha, será gerada uma senha temporária
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserInternalRequest {
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String documentNumber;
    private String role; // ADMIN, TEACHER, STUDENT, PARENT
}

