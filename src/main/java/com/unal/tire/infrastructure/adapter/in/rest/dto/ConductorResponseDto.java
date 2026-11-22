package com.unal.tire.infrastructure.adapter.in.rest.dto;

import com.unal.tire.application.port.in.GestionConductoresUseCase.ConductorVista;
import com.unal.tire.domain.model.PerfilConductor;
import com.unal.tire.domain.valueobject.TipoDocumento;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representacion de salida de un conductor ({@code ConductorResponse}).
 */
public record ConductorResponseDto(
        UUID id,
        String nombres,
        String apellidos,
        String nombreCompleto,
        TipoDocumento tipoDocumento,
        String numeroDocumento,
        String correo,
        String telefono,
        String placaVehiculo,
        UUID flotaId,
        boolean activo,
        boolean tieneViajeEnCurso,
        LocalDateTime creadoEn) {

    public static ConductorResponseDto desde(ConductorVista vista) {
        PerfilConductor p = vista.perfil();
        return new ConductorResponseDto(
                p.getUsuarioId(),
                p.getNombres(),
                p.getApellidos(),
                p.getNombreCompleto(),
                p.getTipoDocumento(),
                p.getNumeroDocumento(),
                vista.correo(),
                p.getTelefono(),
                p.getPlacaVehiculo(),
                p.getFlotaId(),
                p.isActivo(),
                vista.tieneViajeEnCurso(),
                p.getCreadoEn());
    }
}
