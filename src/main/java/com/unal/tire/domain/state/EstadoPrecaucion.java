package com.unal.tire.domain.state;

import com.unal.tire.domain.valueobject.NivelAlerta;

/**
 * Estado {@code PRECAUCION} del nivel de alerta.
 */
public final class EstadoPrecaucion implements EstadoAlertaState {

    @Override
    public NivelAlerta obtenerNivel() {
        return NivelAlerta.PRECAUCION;
    }

    @Override
    public EstadoAlertaState registrarMicrosueno(boolean confirmadoPorConductor, int microsuenosRecientes) {
        return new EstadoCritico();
    }

    @Override
    public EstadoAlertaState confirmarAlerta() {
        return new EstadoNormal();
    }
}
