package com.unal.tire.infrastructure.adapter.in.rest;

import com.unal.tire.application.port.in.ComandoEventoFatiga;
import com.unal.tire.application.port.in.ComandoIniciarViaje;
import com.unal.tire.application.port.in.ComandoTelemetria;
import com.unal.tire.application.port.in.GestionViajesUseCase;
import com.unal.tire.application.port.out.ClaimsAutenticacion;
import com.unal.tire.infrastructure.adapter.in.rest.dto.EventoFatigaDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.IniciarViajeDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.TelemetriaDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.ViajeResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para la gestion de viajes, telemetria y alertas.
 */
@RestController
@RequestMapping("/viajes")
public class ViajeController {

    private final GestionViajesUseCase gestionViajes;

    public ViajeController(GestionViajesUseCase gestionViajes) {
        this.gestionViajes = gestionViajes;
    }

    @PostMapping
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<ViajeResponseDto> iniciar(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @Valid @RequestBody IniciarViajeDto request) {
        var viaje = gestionViajes.iniciarViaje(new ComandoIniciarViaje(
                principal.usuarioId(),
                request.origen(),
                request.destino(),
                request.latitudInicial(),
                request.longitudInicial()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ViajeResponseDto.desde(viaje));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'DESPACHADOR')")
    public ResponseEntity<List<ViajeResponseDto>> listar() {
        List<ViajeResponseDto> viajes = gestionViajes.listarViajes().stream()
                .map(ViajeResponseDto::desde)
                .toList();
        return ResponseEntity.ok(viajes);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'DESPACHADOR')")
    public ResponseEntity<ViajeResponseDto> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(ViajeResponseDto.desde(gestionViajes.obtenerViajePorId(id)));
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<ViajeResponseDto> finalizar(@PathVariable UUID id) {
        return ResponseEntity.ok(ViajeResponseDto.desde(gestionViajes.finalizarViaje(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'DESPACHADOR')")
    public ResponseEntity<Void> cancelar(@PathVariable UUID id) {
        gestionViajes.cancelarViaje(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/telemetria")
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<ViajeResponseDto> registrarTelemetria(
            @PathVariable UUID id,
            @Valid @RequestBody TelemetriaDto request) {
        var viaje = gestionViajes.registrarTelemetria(id, new ComandoTelemetria(
                request.latitud(), request.longitud()));
        return ResponseEntity.ok(ViajeResponseDto.desde(viaje));
    }

    @PostMapping("/{id}/eventos")
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<ViajeResponseDto> registrarEvento(
            @PathVariable UUID id,
            @Valid @RequestBody EventoFatigaDto request) {
        var viaje = gestionViajes.registrarEventoFatiga(id, new ComandoEventoFatiga(
                request.latitud(),
                request.longitud(),
                request.duracionOjosCerradosSegundos(),
                request.confirmadoPorConductor()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ViajeResponseDto.desde(viaje));
    }

    @PatchMapping("/{id}/confirmar-alerta")
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<ViajeResponseDto> confirmarAlerta(@PathVariable UUID id) {
        return ResponseEntity.ok(ViajeResponseDto.desde(gestionViajes.confirmarAlerta(id)));
    }
}
