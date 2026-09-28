package com.silvano.gastos.controller;

import com.silvano.gastos.dto.GastoRequest;
import com.silvano.gastos.dto.GastoResponse;
import com.silvano.gastos.dto.HistoricoResponse;
import com.silvano.gastos.service.GastoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService service;

    public GastoController(GastoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GastoResponse registrar(@RequestAttribute Long usuarioId, @Valid @RequestBody GastoRequest req) {
        return service.registrar(usuarioId, req);
    }

    /** periodo: MES (padrão), 7D, 15D, 30D, 3M, 6M, 12M */
    @GetMapping
    public HistoricoResponse historico(@RequestAttribute Long usuarioId,
                                        @RequestParam(defaultValue = "MES") String periodo) {
        return service.historico(usuarioId, periodo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@RequestAttribute Long usuarioId, @PathVariable Long id) {
        service.excluir(usuarioId, id);
    }
}
