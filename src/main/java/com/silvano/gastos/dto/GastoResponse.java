package com.silvano.gastos.dto;

import com.silvano.gastos.model.Gasto;

import java.math.BigDecimal;
import java.time.Instant;

public record GastoResponse(Long id, BigDecimal valor, String descricao, Instant dataHora) {

    public static GastoResponse de(Gasto g) {
        return new GastoResponse(g.getId(), g.getValor(), g.getDescricao(), g.getDataHora());
    }
}
