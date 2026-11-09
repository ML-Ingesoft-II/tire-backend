package com.unal.tire.domain.state;

import com.unal.tire.domain.valueobject.NivelAlerta;

/**
 * Patron de diseno GoF <b>State</b> aplicado al nivel de alerta de fatiga
 * de un viaje.
 *
 * <p>Cada implementacion encapsula el comportamiento de un nivel concreto
 * ({@code NORMAL}, {@code PRECAUCION}, {@code CRITICO}) y decide la
 * transicion al siguiente estado. Las implementaciones son inmutables y
 * puras: no dependen de ningun framework.</p>
 */
public interface EstadoAlertaState {

    /**
     * @return el nivel de alerta representado por este estado.
     */
    NivelAlerta obtenerNivel();

    /**
     * Calcula la transicion al registrarse un nuevo microsueno.
     *
     * @param confirmadoPorConductor si el conductor confirmo estar alerta.
     * @param microsuenosRecientes   cantidad de microsuenos recientes.
     * @return el estado resultante tras aplicar la regla de transicion.
     */
    EstadoAlertaState registrarMicrosueno(boolean confirmadoPorConductor, int microsuenosRecientes);

    /**
     * Calcula la transicion al confirmar el conductor que esta atento.
     *
     * @return el estado resultante tras la confirmacion.
     */
    EstadoAlertaState confirmarAlerta();
}
