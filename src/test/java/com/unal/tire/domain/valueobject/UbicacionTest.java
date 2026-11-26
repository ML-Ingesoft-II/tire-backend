package com.unal.tire.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.unal.tire.domain.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

class UbicacionTest {

    @Test
    void crear_coordenadasValidas_conservaLatitudYLongitud() {
        // Arrange
        double latitud = 4.6097;
        double longitud = -74.0817;

        // Act
        Ubicacion ubicacion = new Ubicacion(latitud, longitud);

        // Assert
        assertEquals(latitud, ubicacion.latitud());
        assertEquals(longitud, ubicacion.longitud());
    }

    @Test
    void crear_coordenadasEnLosLimitesExactos_esValida() {
        // Arrange
        double latitudMaxima = 90;
        double longitudMinima = -180;

        // Act
        Ubicacion ubicacion = new Ubicacion(latitudMaxima, longitudMinima);

        // Assert
        assertEquals(90, ubicacion.latitud());
        assertEquals(-180, ubicacion.longitud());
    }

    @Test
    void crear_latitudMayorA90_lanzaReglaNegocio() {
        // Arrange
        double latitudInvalida = 90.1;

        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> new Ubicacion(latitudInvalida, 0));

        // Assert
        assertEquals("La latitud debe estar entre -90 y 90 grados.", ex.getMessage());
    }

    @Test
    void crear_longitudMenorA180Negativo_lanzaReglaNegocio() {
        // Arrange
        double longitudInvalida = -180.5;

        // Act
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> new Ubicacion(0, longitudInvalida));

        // Assert
        assertEquals("La longitud debe estar entre -180 y 180 grados.", ex.getMessage());
    }
}
