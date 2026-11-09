package com.unal.tire.infrastructure.adapter.in.rest;

import com.unal.tire.application.port.in.GestionAlertasUseCase;
import com.unal.tire.infrastructure.adapter.in.rest.dto.AlertaCriticaResponseDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.AtenderAlertaDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.EstadoFlotaDto;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para la operacion del despachador: estado de flota
 * y gestion de alertas criticas.
 *
 * <p>Exclusivo del rol DESPACHADOR.</p>
 */
@RestController
@PreAuthorize("hasRole('DESPACHADOR')")
public class FlotaAlertasController {

    private final GestionAlertasUseCase gestionAlertas;

    public FlotaAlertasController(GestionAlertasUseCase gestionAlertas) {
        this.gestionAlertas = gestionAlertas;
    }

    @GetMapping("/flota/estado")
    public ResponseEntity<EstadoFlotaDto> estadoFlota() {
        return ResponseEntity.ok(EstadoFlotaDto.desde(gestionAlertas.listarViajesActivos()));
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaCriticaResponseDto>> alertasAbiertas() {
        List<AlertaCriticaResponseDto> alertas = gestionAlertas.listarAlertasAbiertas().stream()
                .map(AlertaCriticaResponseDto::desde)
                .toList();
        return ResponseEntity.ok(alertas);
    }

    @PatchMapping("/alertas/{id}/atender")
    public ResponseEntity<AlertaCriticaResponseDto> atender(
            @PathVariable UUID id,
            @Valid @RequestBody AtenderAlertaDto request) {
        var alerta = gestionAlertas.atenderAlerta(id, request.notas());
        return ResponseEntity.ok(AlertaCriticaResponseDto.desde(alerta));
    }

    @GetMapping("/incidentes")
    public ResponseEntity<List<AlertaCriticaResponseDto>> incidentesAtendidos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<AlertaCriticaResponseDto> incidentes = gestionAlertas
                .listarIncidentesAtendidos(fechaInicio, fechaFin).stream()
                .map(AlertaCriticaResponseDto::desde)
                .toList();
        return ResponseEntity.ok(incidentes);
    }
}
