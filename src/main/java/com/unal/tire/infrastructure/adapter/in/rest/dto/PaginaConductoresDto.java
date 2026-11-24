package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionConductoresUseCase.PaginaConductores;
import java.util.List;

/**
 * Pagina de conductores ({@code PaginaConductores}).
 */
public record PaginaConductoresDto(
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        List<ConductorResponseDto> contenido) {

    public static PaginaConductoresDto desde(PaginaConductores pagina) {
        return new PaginaConductoresDto(
                pagina.pagina(),
                pagina.tamano(),
                pagina.totalElementos(),
                pagina.totalPaginas(),
                pagina.contenido().stream().map(ConductorResponseDto::desde).toList());
    }
}
