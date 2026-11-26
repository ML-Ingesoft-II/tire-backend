package com.unal.tire.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.unal.tire.domain.valueobject.TipoDocumento;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PerfilConductorTest {

    private PerfilConductor perfilActivo() {
        return new PerfilConductor(UUID.randomUUID(), "Andrés", "Pineda",
                TipoDocumento.values()[0], "1012345678", "3001234567", "ABC123",
                UUID.randomUUID(), true, LocalDateTime.of(2026, 11, 1, 8, 0));
    }

    @Test
    void desactivar_conductorActivo_quedaInactivo() {
        // Arrange
        PerfilConductor perfil = perfilActivo();

        // Act
        perfil.desactivar();

        // Assert
        assertFalse(perfil.isActivo());
    }

    @Test
    void actualizarDatos_conParametrosNulos_conservaLosValoresExistentes() {
        // Arrange
        PerfilConductor perfil = perfilActivo();

        // Act
        perfil.actualizarDatos(null, null, null, null, "3109876543", null, null);

        // Assert
        assertEquals("Andrés", perfil.getNombres());
        assertEquals("ABC123", perfil.getPlacaVehiculo());
        assertEquals("3109876543", perfil.getTelefono());
    }

    @Test
    void getNombreCompleto_conEspaciosSobrantes_losRecorta() {
        // Arrange
        PerfilConductor perfil = perfilActivo();
        perfil.actualizarDatos("  Andrés ", " Pineda  ", null, null, null, null, null);

        // Act
        String nombreCompleto = perfil.getNombreCompleto();

        // Assert
        assertEquals("Andrés Pineda", nombreCompleto);
    }
}
