package com.unal.tire.infrastructure.adapter.in.rest;

import com.unal.tire.application.port.in.ComandoActualizarConductor;
import com.unal.tire.application.port.in.ComandoCrearConductor;
import com.unal.tire.application.port.in.GestionConductoresUseCase;
import com.unal.tire.application.port.out.ClaimsAutenticacion;
import com.unal.tire.infrastructure.adapter.in.rest.dto.ActualizarConductorDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.ConductorResponseDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.CrearConductorDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.PaginaConductoresDto;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para la gestion de conductores de la flota (HU-05,
 * HU-06). Exclusivo del rol DESPACHADOR; el alcance se limita a su flota.
 */
@RestController
@RequestMapping("/conductores")
@PreAuthorize("hasRole('DESPACHADOR')")
public class ConductorController {

    private final GestionConductoresUseCase gestionConductores;

    public ConductorController(GestionConductoresUseCase gestionConductores) {
        this.gestionConductores = gestionConductores;
    }

    @PostMapping
    public ResponseEntity<ConductorResponseDto> crear(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @Valid @RequestBody CrearConductorDto request) {
        var vista = gestionConductores.crear(new ComandoCrearConductor(
                request.nombres(),
                request.apellidos(),
                request.tipoDocumento(),
                request.numeroDocumento(),
                request.correo(),
                request.telefono(),
                request.placaVehiculo(),
                principal.flotaId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ConductorResponseDto.desde(vista));
    }

    @GetMapping
    public ResponseEntity<PaginaConductoresDto> listar(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        var pagina = gestionConductores.listar(principal.flotaId(), q, activo, page, size);
        return ResponseEntity.ok(PaginaConductoresDto.desde(pagina));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConductorResponseDto> obtener(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ConductorResponseDto.desde(
                gestionConductores.obtenerPorId(principal.flotaId(), id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ConductorResponseDto> actualizar(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarConductorDto request) {
        var vista = gestionConductores.actualizar(principal.flotaId(), id, new ComandoActualizarConductor(
                request.nombres(),
                request.apellidos(),
                request.tipoDocumento(),
                request.numeroDocumento(),
                request.correo(),
                request.telefono(),
                request.placaVehiculo(),
                request.activo()));
        return ResponseEntity.ok(ConductorResponseDto.desde(vista));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ConductorResponseDto> desactivar(
            @AuthenticationPrincipal ClaimsAutenticacion principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ConductorResponseDto.desde(
                gestionConductores.desactivar(principal.flotaId(), id)));
    }
}
