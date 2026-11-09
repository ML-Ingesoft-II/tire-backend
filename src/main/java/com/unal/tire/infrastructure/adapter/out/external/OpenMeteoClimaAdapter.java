package com.unal.tire.infrastructure.adapter.out.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unal.tire.application.port.out.ConsultorClimaPort;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adaptador de salida que consulta el clima actual en la API gratuita
 * Open-Meteo y lo traduce al value object {@link CondicionClimatica}.
 *
 * <p>Tolerante a fallos: ante cualquier error (sin internet, timeout, respuesta
 * invalida) devuelve una condicion por defecto para no bloquear al conductor.</p>
 */
@Component
public class OpenMeteoClimaAdapter implements ConsultorClimaPort {

    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private static final double TEMP_POR_DEFECTO = 20.0;
    private static final double VIENTO_POR_DEFECTO = 10.0;
    private static final String DESCRIPCION_SIN_CONEXION = "SIN_CONEXION_METEOROLOGICA";

    private final RestClient restClient;

    public OpenMeteoClimaAdapter() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) TIMEOUT.toMillis());
        requestFactory.setReadTimeout((int) TIMEOUT.toMillis());
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(BASE_URL)
                .build();
    }

    @Override
    public CondicionClimatica consultarClimaActual(double latitud, double longitud) {
        try {
            OpenMeteoResponse respuesta = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("latitude", latitud)
                            .queryParam("longitude", longitud)
                            .queryParam("current", "temperature_2m,wind_speed_10m,weather_code")
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponse.class);

            if (respuesta == null || respuesta.current() == null) {
                return climaPorDefecto();
            }

            OpenMeteoCurrent current = respuesta.current();
            return new CondicionClimatica(
                    current.temperature2m(),
                    current.windSpeed10m(),
                    describirCodigo(current.weatherCode()));
        } catch (Exception ex) {
            return climaPorDefecto();
        }
    }

    private CondicionClimatica climaPorDefecto() {
        return new CondicionClimatica(TEMP_POR_DEFECTO, VIENTO_POR_DEFECTO, DESCRIPCION_SIN_CONEXION);
    }

    /**
     * Traduce el codigo WMO de Open-Meteo a una descripcion corta.
     * Simplificado para el MVP.
     */
    private String describirCodigo(Integer weatherCode) {
        if (weatherCode == null) {
            return "DESCONOCIDO";
        }
        return switch (weatherCode) {
            case 0 -> "DESPEJADO";
            case 1, 2, 3 -> "PARCIALMENTE_NUBLADO";
            case 45, 48 -> "NIEBLA";
            case 51, 53, 55, 56, 57 -> "LLOVIZNA";
            case 61, 63, 65, 66, 67 -> "LLUVIA";
            case 71, 73, 75, 77 -> "NIEVE";
            case 80, 81, 82 -> "CHUBASCOS";
            case 95, 96, 99 -> "TORMENTA";
            default -> "DESCONOCIDO";
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenMeteoResponse(@JsonProperty("current") OpenMeteoCurrent current) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenMeteoCurrent(
            @JsonProperty("temperature_2m") double temperature2m,
            @JsonProperty("wind_speed_10m") double windSpeed10m,
            @JsonProperty("weather_code") Integer weatherCode) {
    }
}
