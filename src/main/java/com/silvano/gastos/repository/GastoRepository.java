package com.silvano.gastos.repository;

import com.silvano.gastos.model.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface GastoRepository extends JpaRepository<Gasto, Long> {

    List<Gasto> findByDataHoraGreaterThanEqualOrderByDataHoraDesc(Instant inicio);
}
