package com.distrischool.template.dto.auth;

import com.distrischool.template.entity.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

/**
 * DTO para requisição de registro de usuário.
 * O backend criará o usuário no Auth0 e sincronizará com o banco local.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
        message = "Senha deve conter pelo menos: 1 letra minúscula, 1 maiúscula, 1 número e 1 caractere especial"
    )
    private String password;

    @NotBlank(message = "Confirmação de senha é obrigatória")
    private String confirmPassword;

    @NotBlank(message = "Primeiro nome é obrigatório")
    @Size(max = 100, message = "Primeiro nome deve ter no máximo 100 caracteres")
    private String firstName;

    @NotBlank(message = "Sobrenome é obrigatório")
    @Size(max = 100, message = "Sobrenome deve ter no máximo 100 caracteres")
    private String lastName;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String phone;

    @Size(max = 50, message = "Número do documento deve ter no máximo 50 caracteres")
    private String documentNumber;

    @NotEmpty(message = "Pelo menos uma role deve ser especificada")
    private Set<UserRole> roles;

    /**
     * Verifica se as senhas coincidem
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}

