package com.unal.tire.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
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

    @Test
    void gettersSettersYEquals_funcionanCorrectamente() {
        UUID id = UUID.randomUUID();
        UUID viajeId = UUID.randomUUID();
        UUID conductorId = UUID.randomUUID();
        UUID flotaId = UUID.randomUUID();
        UUID eventoId = UUID.randomUUID();
        LocalDateTime fecha = LocalDateTime.now();
        Ubicacion u = new Ubicacion(4.5, -74.1);

        AlertaCritica a1 = new AlertaCritica(
                id, viajeId, conductorId, flotaId, eventoId,
                com.unal.tire.domain.valueobject.TipoEvento.MICROSUENO,
                2.1, fecha, fecha, null, u, false, null, null, null
        );

        assertEquals(id, a1.getId());
        assertEquals(viajeId, a1.getViajeId());
        assertEquals(conductorId, a1.getConductorId());
        assertEquals(flotaId, a1.getFlotaId());
        assertEquals(eventoId, a1.getEventoId());
        assertEquals(com.unal.tire.domain.valueobject.TipoEvento.MICROSUENO, a1.getTipoEvento());
        assertEquals(2.1, a1.getDuracionSegundos());
        assertEquals(fecha, a1.getFechaEvento());
        assertEquals(fecha, a1.getFechaApertura());
        assertEquals(u, a1.getUltimaUbicacion());

        AlertaCritica a2 = new AlertaCritica();
        a2.setId(id);
        a2.setViajeId(viajeId);
        a2.setConductorId(conductorId);
        a2.setFlotaId(flotaId);
        a2.setEventoId(eventoId);
        a2.setTipoEvento(com.unal.tire.domain.valueobject.TipoEvento.ROSTRO_NO_VISIBLE);
        a2.setDuracionSegundos(5.5);
        a2.setFechaEvento(fecha);
        a2.setFechaApertura(fecha);
        a2.setUltimaUbicacion(u);
        a2.setAtendida(true);
        a2.setFechaAtencion(fecha);
        a2.setAtendidaPorId(conductorId);
        a2.setAtendidaPorNombre("Despachador");
        a2.setNotasDespachador("Notas");

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
        assertEquals(a1, a1);
        org.junit.jupiter.api.Assertions.assertNotEquals(a1, null);
        org.junit.jupiter.api.Assertions.assertNotEquals(a1, "otro");
    }
}
