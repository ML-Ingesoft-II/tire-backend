package com.unal.tire.infrastructure.adapter.in.rest;

import com.unal.tire.application.port.in.ComandoConfirmarAlerta;
import com.unal.tire.application.port.in.ComandoEventoFatiga;
import com.unal.tire.application.port.in.ComandoIniciarViaje;
import com.unal.tire.application.port.in.ComandoTelemetria;
import com.unal.tire.application.port.in.GestionViajesUseCase;
import com.unal.tire.application.port.out.ClaimsAutenticacion;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.infrastructure.adapter.in.rest.dto.ConfirmarAlertaDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.EventoFatigaDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.IniciarViajeDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.PaginaViajesDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.TelemetriaDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.ViajeResponseDto;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para la gestion de viajes, telemetria y eventos
 * de fatiga (HU-09, HU-10, HU-16 a HU-18, HU-25, HU-26).
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
    public ResponseEntity<PaginaViajesDto> listar(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @RequestParam(required = false) EstadoViaje estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        var pagina = gestionViajes.listarViajes(
                principal.usuarioId(), principal.rol(), principal.flotaId(), estado, page, size);
        return ResponseEntity.ok(PaginaViajesDto.desde(pagina));
    }

    @GetMapping("/activo")
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<ViajeResponseDto> viajeActivo(
            @AuthenticationPrincipal ClaimsAutenticacion principal) {
        return ResponseEntity.ok(ViajeResponseDto.desde(
                gestionViajes.obtenerViajeActivo(principal.usuarioId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'DESPACHADOR')")
    public ResponseEntity<ViajeResponseDto> obtener(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ViajeResponseDto.desde(
                gestionViajes.obtenerViajePorId(
                        principal.usuarioId(), principal.rol(), principal.flotaId(), id)));
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<ViajeResponseDto> finalizar(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ViajeResponseDto.desde(
                gestionViajes.finalizarViaje(principal.usuarioId(), id)));
    }

    @PostMapping("/{id}/telemetria")
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<ViajeResponseDto> registrarTelemetria(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @PathVariable UUID id,
            @Valid @RequestBody TelemetriaDto request) {
        var viaje = gestionViajes.registrarTelemetria(principal.usuarioId(), id,
                new ComandoTelemetria(request.latitud(), request.longitud(), request.velocidadKmh()));
        return ResponseEntity.ok(ViajeResponseDto.desde(viaje));
    }

    @PostMapping("/{id}/eventos")
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<ViajeResponseDto> registrarEvento(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @PathVariable UUID id,
            @Valid @RequestBody EventoFatigaDto request) {
        var viaje = gestionViajes.registrarEventoFatiga(principal.usuarioId(), id,
                new ComandoEventoFatiga(
                        request.tipoEvento(),
                        request.fechaHoraDeteccion(),
                        request.duracionSegundos(),
                        request.respondio(),
                        request.tiempoRespuestaSegundos(),
                        request.latitud(),
                        request.longitud()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ViajeResponseDto.desde(viaje));
    }

    @PatchMapping("/{id}/confirmar-alerta")
    @PreAuthorize("hasRole('CONDUCTOR')")
    public ResponseEntity<ViajeResponseDto> confirmarAlerta(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @PathVariable UUID id,
            @Valid @RequestBody ConfirmarAlertaDto request) {
        var viaje = gestionViajes.confirmarAlerta(principal.usuarioId(), id,
                new ComandoConfirmarAlerta(request.tiempoRespuestaSegundos()));
        return ResponseEntity.ok(ViajeResponseDto.desde(viaje));
    }
}
