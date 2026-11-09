package com.unal.tire.domain.state;

import com.unal.tire.domain.valueobject.NivelAlerta;

/**
 * Estado {@code CRITICO} del nivel de alerta.
 */
public final class EstadoCritico implements EstadoAlertaState {

    @Override
    public NivelAlerta obtenerNivel() {
        return NivelAlerta.CRITICO;
    }

    @Override
    public EstadoAlertaState registrarMicrosueno(boolean confirmadoPorConductor, int microsuenosRecientes) {
        return this;
    }

    @Override
    public EstadoAlertaState confirmarAlerta() {
        return new EstadoPrecaucion();
    }
}
