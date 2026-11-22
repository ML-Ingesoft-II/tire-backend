package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionAlertasUseCase.EntradaLineaTiempo;
import java.time.LocalDateTime;

/**
 * Entrada de la linea de tiempo de una alerta ({@code EntradaLineaTiempo}).
 */
public record EntradaLineaTiempoDto(
        LocalDateTime fechaHora,
        String tipo,
        String descripcion) {

    public static EntradaLineaTiempoDto desde(EntradaLineaTiempo e) {
        return new EntradaLineaTiempoDto(e.fechaHora(), e.tipo(), e.descripcion());
    }
}
