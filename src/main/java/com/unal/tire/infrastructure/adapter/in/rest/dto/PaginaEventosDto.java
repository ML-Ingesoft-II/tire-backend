package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionAlertasUseCase.PaginaEventos;
import java.util.List;

/**
 * Pagina del historial de eventos ({@code PaginaEventos}).
 */
public record PaginaEventosDto(
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        List<EventoHistorialDto> contenido) {

    public static PaginaEventosDto desde(PaginaEventos pagina) {
        return new PaginaEventosDto(
                pagina.pagina(),
                pagina.tamano(),
                pagina.totalElementos(),
                pagina.totalPaginas(),
                pagina.contenido().stream().map(EventoHistorialDto::desde).toList());
    }
}
