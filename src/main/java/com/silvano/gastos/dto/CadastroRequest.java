package com.silvano.gastos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
        @NotBlank(message = "Informe seu nome")
        @Size(max = 100, message = "Nome muito longo")
        String nome,

        @NotBlank(message = "Informe seu e-mail")
        @Email(message = "E-mail inválido")
        @Size(max = 180)
        String email,

        @NotBlank(message = "Informe uma senha")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String senha
) {}
