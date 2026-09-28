package com.silvano.gastos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetarSenhaRequest(
        @NotBlank(message = "Link de redefinição inválido")
        String token,

        @NotBlank(message = "Informe uma senha")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String novaSenha
) {}
