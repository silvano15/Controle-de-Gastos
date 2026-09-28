package com.silvano.gastos.repository;

import com.silvano.gastos.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByUsuarioIdAndDataHoraGreaterThanEqualOrderByDataHoraDesc(Long usuarioId, Instant inicio);

    Optional<Gasto> findByIdAndUsuarioId(Long id, Long usuarioId);
}
