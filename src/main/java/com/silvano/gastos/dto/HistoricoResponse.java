package com.silvano.gastos.dto;

import java.math.BigDecimal;
import java.util.List;

public record HistoricoResponse(String periodo, BigDecimal total, int quantidade, List<GastoResponse> gastos) {}
