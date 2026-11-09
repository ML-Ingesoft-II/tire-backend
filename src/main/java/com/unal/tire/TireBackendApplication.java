package com.unal.tire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend del sistema Tire.
 *
 * <p>Tire previene accidentes viales por microsuenos en conductores de
 * transporte de carga y particulares, conectando una App Movil (CONDUCTOR)
 * y un Panel Web (DESPACHADOR) a este unico backend.</p>
 */
@SpringBootApplication
public class TireBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TireBackendApplication.class, args);
    }
}
