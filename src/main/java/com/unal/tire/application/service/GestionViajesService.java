package com.unal.tire.application.service;

import com.unal.tire.application.port.in.ComandoConfirmarAlerta;
import com.unal.tire.application.port.in.ComandoEventoFatiga;
import com.unal.tire.application.port.in.ComandoIniciarViaje;
import com.unal.tire.application.port.in.ComandoTelemetria;
import com.unal.tire.application.port.in.GestionViajesUseCase;
import com.unal.tire.application.port.out.AlertaCriticaRepositoryPort;
import com.unal.tire.application.port.out.ConsultorClimaPort;
import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.application.port.out.ViajeRepositoryPort;
import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import com.unal.tire.domain.valueobject.RolUsuario;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de gestion de viajes, telemetria y eventos de fatiga.
 *
 * <p>Orquesta el agregado {@link Viaje}, la consulta de clima externo
 * ({@link ConsultorClimaPort}) y la apertura automatica de alertas criticas
 * (solo para conductores de flota).</p>
 */
@Service
public class GestionViajesService implements GestionViajesUseCase {

    private final ViajeRepositoryPort viajeRepository;
    private final AlertaCriticaRepositoryPort alertaRepository;
    private final ConsultorClimaPort consultorClima;
    private final UsuarioRepositoryPort usuarioRepository;

    public GestionViajesService(
            ViajeRepositoryPort viajeRepository,
            AlertaCriticaRepositoryPort alertaRepository,
            ConsultorClimaPort consultorClima,
            UsuarioRepositoryPort usuarioRepository) {
        this.viajeRepository = viajeRepository;
        this.alertaRepository = alertaRepository;
        this.consultorClima = consultorClima;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public Viaje iniciarViaje(ComandoIniciarViaje comando) {
        // HU-09: un conductor no puede tener dos viajes en curso.
        viajeRepository.buscarEnCursoDeConductor(comando.conductorId()).ifPresent(v -> {
            throw new ViajeActivoException(v.getId());
        });

        LocalDateTime ahora = LocalDateTime.now();
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
                ahora,
                null,
                ubicacionInicial,
                climaInicial,
                ahora,
                null,
                new ArrayList<>());

        return viajeRepository.guardar(viaje);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaViajes listarViajes(UUID usuarioId, RolUsuario rol, UUID flotaId,
                                     EstadoViaje estado, int page, int size) {
        List<Viaje> viajes;
        if (rol == RolUsuario.DESPACHADOR) {
            List<UUID> conductores = usuarioRepository.listarPorFlotaYRol(flotaId, RolUsuario.CONDUCTOR)
                    .stream().map(Usuario::getId).toList();
            viajes = viajeRepository.listarPorConductores(conductores);
        } else {
            viajes = viajeRepository.listarPorConductor(usuarioId);
        }

        List<Viaje> filtrados = viajes.stream()
                .filter(v -> estado == null || v.getEstado() == estado)
                .toList();

        long totalElementos = filtrados.size();
        int totalPaginas = size > 0 ? (int) Math.ceil((double) totalElementos / size) : 0;
        int desde = Math.min(page * size, filtrados.size());
        int hasta = Math.min(desde + size, filtrados.size());
        List<Viaje> contenido = filtrados.subList(desde, hasta);

        return new PaginaViajes(page, size, totalElementos, totalPaginas, contenido);
    }

    @Override
    @Transactional(readOnly = true)
    public Viaje obtenerViajeActivo(UUID conductorId) {
        return viajeRepository.buscarEnCursoDeConductor(conductorId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El conductor no tiene un viaje en curso"));
    }

    @Override
    @Transactional(readOnly = true)
    public Viaje obtenerViajePorId(UUID usuarioId, RolUsuario rol, UUID flotaId, UUID id) {
        Viaje viaje = cargarViaje(id);
        exigirAlcance(viaje, usuarioId, rol, flotaId);
        return viaje;
    }

    @Override
    @Transactional
    public Viaje finalizarViaje(UUID conductorId, UUID id) {
        Viaje viaje = cargarViajeDeConductor(conductorId, id);
        viaje.finalizar(LocalDateTime.now());
        return viajeRepository.guardar(viaje);
    }

    @Override
    @Transactional
    public Viaje registrarTelemetria(UUID conductorId, UUID viajeId, ComandoTelemetria comando) {
        Viaje viaje = cargarViajeDeConductor(conductorId, viajeId);

        Ubicacion ubicacion = new Ubicacion(comando.latitud(), comando.longitud());
        CondicionClimatica clima = consultorClima.consultarClimaActual(
                comando.latitud(), comando.longitud());

        viaje.registrarTelemetria(ubicacion, comando.velocidadKmh(), clima, LocalDateTime.now());
        return viajeRepository.guardar(viaje);
    }

    @Override
    @Transactional
    public Viaje registrarEventoFatiga(UUID conductorId, UUID viajeId, ComandoEventoFatiga comando) {
        Viaje viaje = cargarViajeDeConductor(conductorId, viajeId);

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

        NivelAlerta nivelAntes = viaje.getNivelAlerta();

        EventoFatiga evento = new EventoFatiga(
                UUID.randomUUID(),
                viajeId,
                comando.tipoEvento(),
                comando.fechaHoraDeteccion() != null ? comando.fechaHoraDeteccion() : LocalDateTime.now(),
                ubicacion,
                comando.duracionSegundos(),
                comando.tipoEvento() == TipoEvento.MICROSUENO ? comando.respondio() : null,
                comando.tiempoRespuestaSegundos(),
                clima);

        viaje.registrarEventoFatiga(evento);
        Viaje viajeGuardado = viajeRepository.guardar(viaje);

        // Cada paso a CRITICO abre una sola alerta, y solo para conductores de flota.
        if (nivelAntes != NivelAlerta.CRITICO
                && viajeGuardado.getNivelAlerta() == NivelAlerta.CRITICO) {
            abrirAlertaCritica(viajeGuardado, evento);
        }

        return viajeGuardado;
    }

    @Override
    @Transactional
    public Viaje confirmarAlerta(UUID conductorId, UUID viajeId, ComandoConfirmarAlerta comando) {
        Viaje viaje = cargarViajeDeConductor(conductorId, viajeId);
        viaje.confirmarAlertaConductor();
        return viajeRepository.guardar(viaje);
    }

    // ----- Helpers -----

    private void abrirAlertaCritica(Viaje viaje, EventoFatiga evento) {
        UUID flotaId = usuarioRepository.buscarPorId(viaje.getConductorId())
                .map(Usuario::getFlotaId)
                .orElse(null);
        if (flotaId == null) {
            // Conductor particular: no se abren alertas para el panel.
            return;
        }

        AlertaCritica alerta = new AlertaCritica(
                UUID.randomUUID(),
                viaje.getId(),
                viaje.getConductorId(),
                flotaId,
                evento.getId(),
                evento.getTipoEvento(),
                evento.getDuracionSegundos(),
                evento.getFechaHora(),
                LocalDateTime.now(),
                null,
                viaje.getUltimaUbicacion(),
                false,
                null,
                null,
                null);
        alertaRepository.guardar(alerta);
    }

    private Viaje cargarViaje(UUID id) {
        return viajeRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Viaje no encontrado"));
    }

    private Viaje cargarViajeDeConductor(UUID conductorId, UUID id) {
        Viaje viaje = cargarViaje(id);
        if (!viaje.getConductorId().equals(conductorId)) {
            // Fuera de alcance: se responde 404 (no revelar existencia).
            throw new RecursoNoEncontradoException("Viaje no encontrado");
        }
        return viaje;
    }

    private void exigirAlcance(Viaje viaje, UUID usuarioId, RolUsuario rol, UUID flotaId) {
        if (rol == RolUsuario.CONDUCTOR) {
            if (!viaje.getConductorId().equals(usuarioId)) {
                throw new RecursoNoEncontradoException("Viaje no encontrado");
            }
            return;
        }
        // DESPACHADOR: el conductor del viaje debe pertenecer a su flota.
        UUID flotaViaje = usuarioRepository.buscarPorId(viaje.getConductorId())
                .map(Usuario::getFlotaId)
                .orElse(null);
        if (flotaId == null || !flotaId.equals(flotaViaje)) {
            throw new RecursoNoEncontradoException("Viaje no encontrado");
        }
    }

    /**
     * Excepcion interna que transporta el id del viaje en curso para que el
     * adaptador REST responda 409 con {@code viajeId} (ProblemViajeActivo).
     */
    public static class ViajeActivoException extends ConflictoEstadoException {
        private final transient UUID viajeId;

        public ViajeActivoException(UUID viajeId) {
            super("El conductor ya tiene un viaje activo en curso");
            this.viajeId = viajeId;
        }

        public UUID getViajeId() {
            return viajeId;
        }
    }
}
