package com.unal.tire.application.service;

import com.unal.tire.application.port.in.ComandoEventoFatiga;
import com.unal.tire.application.port.in.ComandoIniciarViaje;
import com.unal.tire.application.port.in.ComandoTelemetria;
import com.unal.tire.application.port.in.GestionViajesUseCase;
import com.unal.tire.application.port.out.AlertaCriticaRepositoryPort;
import com.unal.tire.application.port.out.ConsultorClimaPort;
import com.unal.tire.application.port.out.ViajeRepositoryPort;
import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de gestion de viajes, telemetria y alertas criticas.
 *
 * <p>Orquesta el agregado {@link Viaje}, la consulta de clima externo
 * ({@link ConsultorClimaPort}) y la apertura automatica de alertas criticas.</p>
 */
@Service
public class GestionViajesService implements GestionViajesUseCase {

    private final ViajeRepositoryPort viajeRepository;
    private final AlertaCriticaRepositoryPort alertaRepository;
    private final ConsultorClimaPort consultorClima;

    public GestionViajesService(
            ViajeRepositoryPort viajeRepository,
            AlertaCriticaRepositoryPort alertaRepository,
            ConsultorClimaPort consultorClima) {
        this.viajeRepository = viajeRepository;
        this.alertaRepository = alertaRepository;
        this.consultorClima = consultorClima;
    }

    @Override
    @Transactional
    public Viaje iniciarViaje(ComandoIniciarViaje comando) {
        // Regla critica HU-09: un conductor no puede tener dos viajes activos.
        if (viajeRepository.existeViajeEnCursoParaConductor(comando.conductorId())) {
            throw new ConflictoEstadoException("El conductor ya tiene un viaje activo en curso");
        }

        Ubicacion ubicacionInicial = null;
        CondicionClimatica climaInicial = null;
        if (comando.latitudInicial() != null && comando.longitudInicial() != null) {
            ubicacionInicial = new Ubicacion(comando.latitudInicial(), comando.longitudInicial());
            climaInicial = consultorClima.consultarClimaActual(
                    comando.latitudInicial(), comando.longitudInicial());
        }

        Viaje viaje = new Viaje(
                UUID.randomUUID(),
                comando.conductorId(),
                comando.origen(),
                comando.destino(),
                EstadoViaje.EN_CURSO,
                NivelAlerta.NORMAL,
                LocalDateTime.now(),
                null,
                ubicacionInicial,
                climaInicial,
                new ArrayList<>());

        return viajeRepository.guardar(viaje);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Viaje> listarViajes() {
        return viajeRepository.listarTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public Viaje obtenerViajePorId(UUID id) {
        return cargarViaje(id);
    }

    @Override
    @Transactional
    public Viaje finalizarViaje(UUID id) {
        Viaje viaje = cargarViaje(id);
        viaje.finalizar(LocalDateTime.now());
        return viajeRepository.guardar(viaje);
    }

    @Override
    @Transactional
    public void cancelarViaje(UUID id) {
        Viaje viaje = cargarViaje(id);
        viaje.cancelar();
        viajeRepository.guardar(viaje);
    }

    @Override
    @Transactional
    public Viaje registrarTelemetria(UUID viajeId, ComandoTelemetria comando) {
        Viaje viaje = cargarViaje(viajeId);

        Ubicacion ubicacion = new Ubicacion(comando.latitud(), comando.longitud());
        CondicionClimatica clima = consultorClima.consultarClimaActual(
                comando.latitud(), comando.longitud());

        viaje.registrarTelemetria(ubicacion, clima);
        return viajeRepository.guardar(viaje);
    }

    @Override
    @Transactional
    public Viaje registrarEventoFatiga(UUID viajeId, ComandoEventoFatiga comando) {
        Viaje viaje = cargarViaje(viajeId);

        Ubicacion ubicacion = null;
        CondicionClimatica clima;
        if (comando.latitud() != null && comando.longitud() != null) {
            ubicacion = new Ubicacion(comando.latitud(), comando.longitud());
            clima = consultorClima.consultarClimaActual(comando.latitud(), comando.longitud());
        } else if (viaje.getUltimaUbicacion() != null) {
            Ubicacion ultima = viaje.getUltimaUbicacion();
            clima = consultorClima.consultarClimaActual(ultima.latitud(), ultima.longitud());
        } else {
            clima = viaje.getUltimoClima();
        }

        EventoFatiga evento = new EventoFatiga(
                UUID.randomUUID(),
                viajeId,
                LocalDateTime.now(),
                ubicacion,
                comando.duracionOjosCerradosSegundos(),
                comando.confirmadoPorConductor(),
                clima);

        viaje.registrarEventoFatiga(evento);
        Viaje viajeGuardado = viajeRepository.guardar(viaje);

        // Si el viaje escalo a CRITICO, se abre una alerta para el despachador.
        if (viajeGuardado.getNivelAlerta() == NivelAlerta.CRITICO) {
            abrirAlertaCritica(viajeGuardado);
        }

        return viajeGuardado;
    }

    @Override
    @Transactional
    public Viaje confirmarAlerta(UUID viajeId) {
        Viaje viaje = cargarViaje(viajeId);
        viaje.confirmarAlertaConductor();
        return viajeRepository.guardar(viaje);
    }

    private void abrirAlertaCritica(Viaje viaje) {
        AlertaCritica alerta = new AlertaCritica(
                UUID.randomUUID(),
                viaje.getId(),
                viaje.getConductorId(),
                null,
                LocalDateTime.now(),
                null,
                viaje.getUltimaUbicacion(),
                false,
                null);
        alertaRepository.guardar(alerta);
    }

    private Viaje cargarViaje(UUID id) {
        return viajeRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Viaje no encontrado"));
    }
}
