package com.silvano.gastos.service;

import com.silvano.gastos.dto.GastoRequest;
import com.silvano.gastos.dto.GastoResponse;
import com.silvano.gastos.dto.HistoricoResponse;
import com.silvano.gastos.model.Gasto;
import com.silvano.gastos.repository.GastoRepository;
import com.silvano.gastos.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class GastoService {

    private final GastoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final ZoneId zona;

    public GastoService(GastoRepository repository, UsuarioRepository usuarioRepository,
                         @Value("${app.timezone}") String timezone) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.zona = ZoneId.of(timezone);
    }

    public GastoResponse registrar(Long usuarioId, GastoRequest req) {
        Gasto gasto = new Gasto();
        gasto.setUsuario(usuarioRepository.getReferenceById(usuarioId));
        gasto.setValor(req.valor().setScale(2, RoundingMode.HALF_UP));
        String desc = req.descricao() == null ? null : req.descricao().trim();
        gasto.setDescricao(desc == null || desc.isEmpty() ? null : desc);
        gasto.setDataHora(Instant.now()); // o sistema pega a data e hora sozinho
        return GastoResponse.de(repository.save(gasto));
    }

    public HistoricoResponse historico(Long usuarioId, String periodo) {
        Instant inicio = calcularInicio(periodo);
        List<GastoResponse> gastos = repository.findByUsuarioIdAndDataHoraGreaterThanEqualOrderByDataHoraDesc(usuarioId, inicio)
                .stream().map(GastoResponse::de).toList();
        BigDecimal total = gastos.stream().map(GastoResponse::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new HistoricoResponse(periodo, total, gastos.size(), gastos);
    }

    public void excluir(Long usuarioId, Long id) {
        Gasto gasto = repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gasto não encontrado"));
        repository.delete(gasto);
    }

    /** Converte o filtro escolhido na tela em uma data de início. */
    private Instant calcularInicio(String periodo) {
        ZonedDateTime agora = ZonedDateTime.now(zona);
        LocalDate hoje = agora.toLocalDate();
        LocalDate inicio = switch (periodo == null ? "MES" : periodo.toUpperCase()) {
            case "7D"  -> hoje.minusDays(6);
            case "15D" -> hoje.minusDays(14);
            case "30D" -> hoje.minusDays(29);
            case "3M"  -> hoje.minusMonths(3);
            case "6M"  -> hoje.minusMonths(6);
            case "12M" -> hoje.minusMonths(12);
            default    -> hoje.withDayOfMonth(1); // "MES": mês atual
        };
        return inicio.atStartOfDay(zona).toInstant();
    }
}
