package com.unal.tire.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unal.tire.domain.exception.ConflictoEstadoException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AlertaCriticaTest {

    @Test
    void atender_alertaPendiente_quedaAtendidaConResponsableYNotas() {
        // Arrange
        AlertaCritica alerta = new AlertaCritica();
        UUID despachadorId = UUID.randomUUID();

        // Act
        alerta.atender(despachadorId, "Laura Gómez", "Llamé al conductor, se detuvo a descansar");

        // Assert
        assertTrue(alerta.isAtendida());
        assertEquals(despachadorId, alerta.getAtendidaPorId());
        assertEquals("Laura Gómez", alerta.getAtendidaPorNombre());
        assertEquals("Llamé al conductor, se detuvo a descansar", alerta.getNotasDespachador());
        assertNotNull(alerta.getFechaAtencion());
    }

    @Test
    void atender_alertaYaAtendida_lanzaConflictoYConservaElPrimerResponsable() {
        // Arrange
        AlertaCritica alerta = new AlertaCritica();
        UUID primerDespachador = UUID.randomUUID();
        alerta.atender(primerDespachador, "Laura Gómez", "primera atención");

        // Act
        assertThrows(ConflictoEstadoException.class,
                () -> alerta.atender(UUID.randomUUID(), "Otro despachador", "segunda atención"));

        // Assert
        assertEquals(primerDespachador, alerta.getAtendidaPorId());
        assertEquals("primera atención", alerta.getNotasDespachador());
    }
}
