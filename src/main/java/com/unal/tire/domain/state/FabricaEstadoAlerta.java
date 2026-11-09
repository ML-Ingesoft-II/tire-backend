package com.unal.tire.domain.state;

import com.unal.tire.domain.valueobject.NivelAlerta;

/**
 * Fabrica (helper estatico) que reconstruye la instancia de
 * {@link EstadoAlertaState} correspondiente a un {@link NivelAlerta}.
 *
 * <p>Permite rehidratar el estado del patron State a partir del nivel
 * persistido en el agregado {@code Viaje}.</p>
 */
public final class FabricaEstadoAlerta {

    private FabricaEstadoAlerta() {
        // Clase utilitaria: no instanciable.
    }

    /**
     * @param nivel nivel de alerta persistido.
     * @return la instancia de estado concreta correspondiente.
     */
    public static EstadoAlertaState desdeNivel(NivelAlerta nivel) {
        return switch (nivel) {
            case NORMAL -> new EstadoNormal();
            case PRECAUCION -> new EstadoPrecaucion();
            case CRITICO -> new EstadoCritico();
        };
    }
}
