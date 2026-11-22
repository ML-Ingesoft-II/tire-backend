package com.unal.tire.infrastructure.adapter.in.rest;

import com.unal.tire.application.port.in.GestionAlertasUseCase;
import com.unal.tire.application.port.out.ClaimsAutenticacion;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.infrastructure.adapter.in.rest.dto.AlertaDetalleResponseDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.AlertaResponseDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.AtenderAlertaDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.EstadoFlotaDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.PaginaEventosDto;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para la operacion del despachador: estado de flota,
 * alertas criticas e historial de incidentes (HU-20, HU-23, HU-24).
 *
 * <p>Exclusivo del rol DESPACHADOR; alcance por la flota del token.</p>
 */
@RestController
@PreAuthorize("hasRole('DESPACHADOR')")
public class FlotaAlertasController {

    private final GestionAlertasUseCase gestionAlertas;

    public FlotaAlertasController(GestionAlertasUseCase gestionAlertas) {
        this.gestionAlertas = gestionAlertas;
    }

    @GetMapping("/flota/estado")
    public ResponseEntity<EstadoFlotaDto> estadoFlota(
            @AuthenticationPrincipal ClaimsAutenticacion principal) {
        return ResponseEntity.ok(EstadoFlotaDto.desde(
                gestionAlertas.obtenerEstadoFlota(principal.flotaId())));
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaResponseDto>> alertasAbiertas(
            @AuthenticationPrincipal ClaimsAutenticacion principal) {
        List<AlertaResponseDto> alertas = gestionAlertas.listarAlertasAbiertas(principal.flotaId())
                .stream()
                .map(AlertaResponseDto::desde)
                .toList();
        return ResponseEntity.ok(alertas);
    }

    @GetMapping("/alertas/{id}")
    public ResponseEntity<AlertaDetalleResponseDto> detalleAlerta(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(AlertaDetalleResponseDto.desde(
                gestionAlertas.obtenerAlerta(principal.flotaId(), id)));
    }

    @PatchMapping("/alertas/{id}/atender")
    public ResponseEntity<AlertaResponseDto> atender(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid AtenderAlertaDto request) {
        String notas = request != null ? request.notas() : null;
        var alerta = gestionAlertas.atenderAlerta(
                principal.flotaId(), principal.usuarioId(), principal.email(), id, notas);
        return ResponseEntity.ok(AlertaResponseDto.desde(alerta));
    }

    @GetMapping("/eventos")
    public ResponseEntity<PaginaEventosDto> historialEventos(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) TipoEvento tipoEvento,
            @RequestParam(required = false) UUID conductorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        var pagina = gestionAlertas.historialEventos(
                principal.flotaId(), fechaInicio, fechaFin, tipoEvento, conductorId, page, size);
        return ResponseEntity.ok(PaginaEventosDto.desde(pagina));
    }
}
