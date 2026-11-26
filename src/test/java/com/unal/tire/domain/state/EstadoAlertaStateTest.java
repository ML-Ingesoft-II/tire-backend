package com.unal.tire.domain.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.unal.tire.domain.valueobject.NivelAlerta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EstadoAlertaStateTest {

    @Test
    void normal_microsuenoConfirmadoUnico_pasaAPrecaucion() {
        // Arrange
        EstadoAlertaState estado = new EstadoNormal();

        // Act
        EstadoAlertaState siguiente = estado.registrarMicrosueno(true, 1);

        // Assert
        assertEquals(NivelAlerta.PRECAUCION, siguiente.obtenerNivel());
    }

    @Test
    void normal_microsuenoNoConfirmado_pasaACritico() {
        // Arrange
        EstadoAlertaState estado = new EstadoNormal();

        // Act
        EstadoAlertaState siguiente = estado.registrarMicrosueno(false, 1);

        // Assert
        assertEquals(NivelAlerta.CRITICO, siguiente.obtenerNivel());
    }

    @Test
    void normal_segundoMicrosuenoAunqueConfirmado_pasaACritico() {
        // Arrange
        EstadoAlertaState estado = new EstadoNormal();

        // Act
        EstadoAlertaState siguiente = estado.registrarMicrosueno(true, 2);

        // Assert
        assertEquals(NivelAlerta.CRITICO, siguiente.obtenerNivel());
    }

    @Test
    void precaucion_nuevoMicrosueno_pasaACritico() {
        // Arrange
        EstadoAlertaState estado = new EstadoPrecaucion();

        // Act
        EstadoAlertaState siguiente = estado.registrarMicrosueno(true, 1);

        // Assert
        assertEquals(NivelAlerta.CRITICO, siguiente.obtenerNivel());
    }

    @Test
    void critico_nuevoMicrosueno_permaneceEnCritico() {
        // Arrange
        EstadoAlertaState estado = new EstadoCritico();

        // Act
        EstadoAlertaState siguiente = estado.registrarMicrosueno(false, 5);

        // Assert
        assertSame(estado, siguiente);
    }

    @Test
    void confirmarAlerta_bajaUnNivelYNormalSeQuedaEnNormal() {
        // Arrange
        EstadoAlertaState critico = new EstadoCritico();
        EstadoAlertaState precaucion = new EstadoPrecaucion();
        EstadoAlertaState normal = new EstadoNormal();

        // Act + Assert
        assertEquals(NivelAlerta.PRECAUCION, critico.confirmarAlerta().obtenerNivel());
        assertEquals(NivelAlerta.NORMAL, precaucion.confirmarAlerta().obtenerNivel());
        assertSame(normal, normal.confirmarAlerta());
    }

    @ParameterizedTest
    @EnumSource(NivelAlerta.class)
    void fabricaDesdeNivel_cadaNivel_devuelveElEstadoCorrespondiente(NivelAlerta nivel) {
        // Arrange + Act
        EstadoAlertaState estado = FabricaEstadoAlerta.desdeNivel(nivel);

        // Assert
        assertEquals(nivel, estado.obtenerNivel());
    }
}
