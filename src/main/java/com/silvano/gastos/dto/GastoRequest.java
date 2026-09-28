package com.silvano.gastos.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record GastoRequest(
        @NotNull(message = "Informe o valor")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @DecimalMax(value = "9999999999.99", message = "Valor muito alto")
        BigDecimal valor,

        @Size(max = 100, message = "Descrição com no máximo 100 caracteres")
        String descricao
) {}
