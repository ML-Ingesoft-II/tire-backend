package com.unal.tire.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unal.tire.domain.exception.ReglaNegocioException;
import com.unal.tire.domain.valueobject.TipoEvento;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventoFatigaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 26, 22, 30);

    private EventoFatiga crear(TipoEvento tipo, LocalDateTime fecha, double duracion, Boolean respondio) {
        return new EventoFatiga(UUID.randomUUID(), UUID.randomUUID(), tipo, fecha,
                null, duracion, respondio, null, null);
    }

    @Test
    void crear_microsuenoEnElUmbralExacto_esValido() {
        // Arrange
        double duracion = EventoFatiga.UMBRAL_MICROSUENO_SEGUNDOS;

        // Act + Assert
        assertDoesNotThrow(() -> crear(TipoEvento.MICROSUENO, AHORA, duracion, false));
    }

    @Test
    void crear_microsuenoPorDebajoDelUmbral_lanzaReglaNegocio() {
        // Arrange
        double duracion = 1.49;

        // Act + Assert
        assertThrows(ReglaNegocioException.class,
                () -> crear(TipoEvento.MICROSUENO, AHORA, duracion, false));
    }

    @Test
    void crear_microsuenoSinIndicarSiRespondio_lanzaReglaNegocio() {
        // Arrange
        Boolean respondio = null;

        // Act + Assert
        assertThrows(ReglaNegocioException.class,
                () -> crear(TipoEvento.MICROSUENO, AHORA, 2.0, respondio));
    }

    @Test
    void crear_sinTipoDeEvento_lanzaReglaNegocio() {
        // Arrange
        TipoEvento tipo = null;

        // Act + Assert
        assertThrows(ReglaNegocioException.class, () -> crear(tipo, AHORA, 2.0, false));
    }

    @Test
    void crear_sinFechaHora_lanzaReglaNegocio() {
        // Arrange
        LocalDateTime fecha = null;

        // Act + Assert
        assertThrows(ReglaNegocioException.class,
                () -> crear(TipoEvento.MICROSUENO, fecha, 2.0, false));
    }

    @Test
    void crear_rostroNoVisibleMenorA5Segundos_lanzaReglaNegocio() {
        // Arrange
        double duracion = 4.9;

        // Act + Assert
        assertThrows(ReglaNegocioException.class,
                () -> crear(TipoEvento.ROSTRO_NO_VISIBLE, AHORA, duracion, null));
    }

    @Test
    void fueConfirmado_microsuenoConRespuestaDelConductor_esVerdadero() {
        // Arrange
        EventoFatiga evento = crear(TipoEvento.MICROSUENO, AHORA, 2.0, true);

        // Act
        boolean confirmado = evento.fueConfirmado();

        // Assert
        assertTrue(confirmado);
    }

    @Test
    void afectaNivelAlerta_rostroNoVisible_esFalso() {
        // Arrange
        EventoFatiga evento = crear(TipoEvento.ROSTRO_NO_VISIBLE, AHORA, 6.0, null);

        // Act
        boolean afecta = evento.afectaNivelAlerta();

        // Assert
        assertFalse(afecta);
        assertFalse(evento.fueConfirmado());
    }
}
