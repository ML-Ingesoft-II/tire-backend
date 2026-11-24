package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionViajesUseCase.PaginaViajes;
import java.util.List;

/**
 * Pagina de viajes ({@code PaginaViajes}). El listado no incluye la lista de
 * eventos de cada viaje.
 */
public record PaginaViajesDto(
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        List<ViajeResponseDto> contenido) {

    public static PaginaViajesDto desde(PaginaViajes pagina) {
        return new PaginaViajesDto(
                pagina.pagina(),
                pagina.tamano(),
                pagina.totalElementos(),
                pagina.totalPaginas(),
                pagina.contenido().stream().map(ViajeResponseDto::desdeSinEventos).toList());
    }
}
