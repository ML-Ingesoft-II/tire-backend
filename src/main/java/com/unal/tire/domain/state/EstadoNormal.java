package com.unal.tire.domain.state;

import com.unal.tire.domain.valueobject.NivelAlerta;

/**
 * Estado {@code NORMAL} del nivel de alerta.
 */
public final class EstadoNormal implements EstadoAlertaState {

    @Override
    public NivelAlerta obtenerNivel() {
        return NivelAlerta.NORMAL;
    }

    @Override
    public EstadoAlertaState registrarMicrosueno(boolean confirmadoPorConductor, int microsuenosRecientes) {
        if (!confirmadoPorConductor || microsuenosRecientes >= 2) {
            return new EstadoCritico();
        }
        return new EstadoPrecaucion();
    }

    @Override
    public EstadoAlertaState confirmarAlerta() {
        return this;
    }
}
