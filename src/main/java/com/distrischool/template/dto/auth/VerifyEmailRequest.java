package com.distrischool.template.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para requisições de verificação de email.
 */
@Data
public class VerifyEmailRequest {

    @NotBlank(message = "Token é obrigatório")
    private String token;
}

