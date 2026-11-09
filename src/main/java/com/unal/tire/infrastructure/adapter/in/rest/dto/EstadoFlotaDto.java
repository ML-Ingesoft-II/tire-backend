package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resumen del estado de la flota para el panel del despachador.
 *
 * @param totalViajesActivos cantidad de viajes en curso.
 * @param conteoPorNivel     cantidad de viajes activos por nivel de alerta.
 * @param viajesActivos      detalle de los viajes activos.
 */
public record EstadoFlotaDto(
        int totalViajesActivos,
        Map<NivelAlerta, Long> conteoPorNivel,
        List<ViajeResponseDto> viajesActivos) {

    public static EstadoFlotaDto desde(List<Viaje> viajesActivos) {
        Map<NivelAlerta, Long> conteo = viajesActivos.stream()
                .collect(Collectors.groupingBy(Viaje::getNivelAlerta, Collectors.counting()));

        List<ViajeResponseDto> detalle = viajesActivos.stream()
                .map(ViajeResponseDto::desde)
                .toList();

        return new EstadoFlotaDto(viajesActivos.size(), conteo, detalle);
    }
}
