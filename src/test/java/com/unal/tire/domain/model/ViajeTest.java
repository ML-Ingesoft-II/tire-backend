package com.unal.tire.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.ReglaNegocioException;
import com.unal.tire.domain.valueobject.EstadoMonitoreo;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ViajeTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 26, 20, 0);

    private Viaje viajeEnCurso() {
        return new Viaje(UUID.randomUUID(), UUID.randomUUID(), "Bogotá", "Tunja",
                EstadoViaje.EN_CURSO, NivelAlerta.NORMAL, INICIO, null,
                null, null, INICIO, null, null);
    }

    private EventoFatiga microsueno(boolean respondio) {
        return new EventoFatiga(UUID.randomUUID(), null, TipoEvento.MICROSUENO,
                INICIO.plusMinutes(30), new Ubicacion(5.0, -73.5), 2.0, respondio, null, null);
    }

    private EventoFatiga rostroNoVisible() {
        return new EventoFatiga(UUID.randomUUID(), null, TipoEvento.ROSTRO_NO_VISIBLE,
                INICIO.plusMinutes(30), null, 6.0, null, null, null);
    }

    @Test
    void registrarEventoFatiga_microsuenoSinRespuesta_escalaACritico() {
        // Arrange
        Viaje viaje = viajeEnCurso();

        // Act
        viaje.registrarEventoFatiga(microsueno(false));

        // Assert
        assertEquals(NivelAlerta.CRITICO, viaje.getNivelAlerta());
        assertEquals(1, viaje.getEventosFatiga().size());
    }

    @Test
    void registrarEventoFatiga_primerMicrosuenoConfirmado_pasaAPrecaucion() {
        // Arrange
        Viaje viaje = viajeEnCurso();

        // Act
        viaje.registrarEventoFatiga(microsueno(true));

        // Assert
        assertEquals(NivelAlerta.PRECAUCION, viaje.getNivelAlerta());
    }

    @Test
    void registrarEventoFatiga_microsuenoConUbicacion_actualizaUltimaUbicacion() {
        // Arrange
        Viaje viaje = viajeEnCurso();
        EventoFatiga evento = microsueno(true);

        // Act
        viaje.registrarEventoFatiga(evento);

        // Assert
        assertEquals(evento.getUbicacion(), viaje.getUltimaUbicacion());
    }

    @Test
    void registrarEventoFatiga_rostroNoVisible_noCambiaElNivelDeAlerta() {
        // Arrange
        Viaje viaje = viajeEnCurso();

        // Act
        viaje.registrarEventoFatiga(rostroNoVisible());

        // Assert
        assertEquals(NivelAlerta.NORMAL, viaje.getNivelAlerta());
        assertEquals(1, viaje.getEventosFatiga().size());
    }

    @Test
    void registrarEventoFatiga_eventoNulo_lanzaReglaNegocio() {
        // Arrange
        Viaje viaje = viajeEnCurso();

        // Act + Assert
        assertThrows(ReglaNegocioException.class, () -> viaje.registrarEventoFatiga(null));
    }

    @Test
    void registrarEventoFatiga_viajeFinalizado_lanzaConflictoYNoGuardaElEvento() {
        // Arrange
        Viaje viaje = viajeEnCurso();
        viaje.finalizar(INICIO.plusHours(3));

        // Act
        assertThrows(ConflictoEstadoException.class,
                () -> viaje.registrarEventoFatiga(microsueno(false)));

        // Assert
        assertTrue(viaje.getEventosFatiga().isEmpty());
        assertEquals(NivelAlerta.NORMAL, viaje.getNivelAlerta());
    }

    @Test
    void confirmarAlertaConductor_desdeCritico_bajaAPrecaucion() {
        // Arrange
        Viaje viaje = viajeEnCurso();
        viaje.registrarEventoFatiga(microsueno(false));

        // Act
        viaje.confirmarAlertaConductor();

        // Assert
        assertEquals(NivelAlerta.PRECAUCION, viaje.getNivelAlerta());
    }

    @Test
    void finalizar_fechaPosteriorAlInicio_quedaFinalizado() {
        // Arrange
        Viaje viaje = viajeEnCurso();
        LocalDateTime fin = INICIO.plusHours(3);

        // Act
        viaje.finalizar(fin);

        // Assert
        assertEquals(EstadoViaje.FINALIZADO, viaje.getEstado());
        assertEquals(fin, viaje.getFechaFin());
    }

    @Test
    void finalizar_fechaAnteriorAlInicio_lanzaReglaNegocioYSigueEnCurso() {
        // Arrange
        Viaje viaje = viajeEnCurso();

        // Act
        assertThrows(ReglaNegocioException.class, () -> viaje.finalizar(INICIO.minusMinutes(1)));

        // Assert
        assertEquals(EstadoViaje.EN_CURSO, viaje.getEstado());
        assertNull(viaje.getFechaFin());
    }

    @Test
    void finalizar_sinFecha_lanzaReglaNegocio() {
        // Arrange
        Viaje viaje = viajeEnCurso();

        // Act + Assert
        assertThrows(ReglaNegocioException.class, () -> viaje.finalizar(null));
    }

    @Test
    void cancelar_viajeEnCursoSinEventos_quedaCancelado() {
        // Arrange
        Viaje viaje = viajeEnCurso();

        // Act
        viaje.cancelar();

        // Assert
        assertEquals(EstadoViaje.CANCELADO, viaje.getEstado());
    }

    @Test
    void cancelar_viajeConEventosDeFatiga_lanzaConflictoEstado() {
        // Arrange
        Viaje viaje = viajeEnCurso();
        viaje.registrarEventoFatiga(rostroNoVisible());

        // Act + Assert
        assertThrows(ConflictoEstadoException.class, viaje::cancelar);
        assertEquals(EstadoViaje.EN_CURSO, viaje.getEstado());
    }

    @Test
    void registrarTelemetria_viajeCancelado_lanzaConflictoEstado() {
        // Arrange
        Viaje viaje = viajeEnCurso();
        viaje.cancelar();

        // Act + Assert
        assertThrows(ConflictoEstadoException.class,
                () -> viaje.registrarTelemetria(new Ubicacion(4.6, -74.0), 80.0, null, INICIO));
    }

    @Test
    void calcularEstadoMonitoreo_senalHaceMasDeDosMinutos_devuelveSinSenal() {
        // Arrange
        Viaje viaje = viajeEnCurso();
        LocalDateTime ahora = INICIO.plusMinutes(10);
        LocalDateTime ultimaSenal = ahora.minusMinutes(2).minusSeconds(1);

        // Act
        EstadoMonitoreo estado = viaje.calcularEstadoMonitoreo(ahora, ultimaSenal);

        // Assert
        assertEquals(EstadoMonitoreo.SIN_SENAL, estado);
    }

    @Test
    void calcularEstadoMonitoreo_senalExactamenteHaceDosMinutos_reflejaElNivelDeAlerta() {
        // Arrange
        Viaje viaje = viajeEnCurso();
        LocalDateTime ahora = INICIO.plusMinutes(10);
        LocalDateTime ultimaSenal = ahora.minusMinutes(Viaje.UMBRAL_SIN_SENAL_MINUTOS);

        // Act
        EstadoMonitoreo estado = viaje.calcularEstadoMonitoreo(ahora, ultimaSenal);

        // Assert
        assertEquals(EstadoMonitoreo.NORMAL, estado);
    }
}
