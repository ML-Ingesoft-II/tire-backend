package com.unal.tire.infrastructure.adapter.in.rest;

import com.unal.tire.application.port.in.ComandoActualizarConductor;
import com.unal.tire.application.port.in.ComandoCrearConductor;
import com.unal.tire.application.port.in.GestionConductoresUseCase;
import com.unal.tire.infrastructure.adapter.in.rest.dto.ActualizarConductorDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.ConductorResponseDto;
import com.unal.tire.infrastructure.adapter.in.rest.dto.CrearConductorDto;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada REST para la gestion de conductores.
 *
 * <p>Exclusivo del rol DESPACHADOR: cualquier intento de un CONDUCTOR produce
 * un 403 gestionado por Spring Security.</p>
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
    public ResponseEntity<ConductorResponseDto> crear(@Valid @RequestBody CrearConductorDto request) {
        var conductor = gestionConductores.crear(new ComandoCrearConductor(
                request.nombre(),
                request.documento(),
                request.correo(),
                request.telefono(),
                request.flotaId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ConductorResponseDto.desde(conductor));
    }

    @GetMapping
    public ResponseEntity<List<ConductorResponseDto>> listar() {
        List<ConductorResponseDto> conductores = gestionConductores.listarTodos().stream()
                .map(ConductorResponseDto::desde)
                .toList();
        return ResponseEntity.ok(conductores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConductorResponseDto> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(ConductorResponseDto.desde(gestionConductores.obtenerPorId(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ConductorResponseDto> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarConductorDto request) {
        var conductor = gestionConductores.actualizar(id, new ComandoActualizarConductor(
                request.nombre(),
                request.telefono(),
                request.activo()));
        return ResponseEntity.ok(ConductorResponseDto.desde(conductor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ConductorResponseDto> desactivar(@PathVariable UUID id) {
        return ResponseEntity.ok(ConductorResponseDto.desde(gestionConductores.desactivar(id)));
    }
}
