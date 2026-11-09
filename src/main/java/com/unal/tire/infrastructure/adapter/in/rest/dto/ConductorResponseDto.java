package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.domain.model.Conductor;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representacion de salida de un conductor.
 */
public record ConductorResponseDto(
        UUID id,
        String nombre,
        String documento,
        String correo,
        String telefono,
        UUID flotaId,
        boolean activo,
        LocalDateTime creadoEn) {

    public static ConductorResponseDto desde(Conductor c) {
        return new ConductorResponseDto(
                c.getId(),
                c.getNombre(),
                c.getDocumento(),
                c.getCorreo(),
                c.getTelefono(),
                c.getFlotaId(),
                c.isActivo(),
                c.getCreadoEn());
    }
}
