package com.distrischool.template.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para requisições de recuperação de senha.
 */
@Data
public class ForgotPasswordRequest {

    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    private String email;
}


