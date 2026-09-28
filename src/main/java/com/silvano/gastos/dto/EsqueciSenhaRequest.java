package com.silvano.gastos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequest(
        @NotBlank(message = "Informe seu e-mail")
        @Email(message = "E-mail inválido")
        String email
) {}
