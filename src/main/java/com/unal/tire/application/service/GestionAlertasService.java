package com.unal.tire.application.service;

import com.unal.tire.application.port.in.GestionAlertasUseCase;
import com.unal.tire.application.port.out.AlertaCriticaRepositoryPort;
import com.unal.tire.application.port.out.ConductorRepositoryPort;
import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.application.port.out.ViajeRepositoryPort;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.exception.ReglaNegocioException;
import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.model.PerfilConductor;
import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.EstadoMonitoreo;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de operacion del despachador: estado de la flota, alertas
 * criticas e historial de incidentes (HU-20, HU-23, HU-24).
 *
 * <p>Todo con alcance por la flota del token.</p>
 */
@Service
public class GestionAlertasService implements GestionAlertasUseCase {

    private final ViajeRepositoryPort viajeRepository;
    private final AlertaCriticaRepositoryPort alertaRepository;
    private final ConductorRepositoryPort conductorRepository;
    private final UsuarioRepositoryPort usuarioRepository;

    public GestionAlertasService(
            ViajeRepositoryPort viajeRepository,
            AlertaCriticaRepositoryPort alertaRepository,
            ConductorRepositoryPort conductorRepository,
            UsuarioRepositoryPort usuarioRepository) {
        this.viajeRepository = viajeRepository;
        this.alertaRepository = alertaRepository;
        this.conductorRepository = conductorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public EstadoFlota obtenerEstadoFlota(UUID flotaId) {
        LocalDateTime ahora = LocalDateTime.now();

        List<PerfilConductor> conductores = conductorRepository.listarActivosPorFlota(flotaId);
        Map<UUID, PerfilConductor> porId = conductores.stream()
                .collect(Collectors.toMap(PerfilConductor::getUsuarioId, p -> p));

        List<UUID> ids = new ArrayList<>(porId.keySet());
        List<Viaje> viajes = viajeRepository.listarPorConductores(ids);

        // Viaje en curso por conductor y alertas abiertas por viaje.
        Map<UUID, Viaje> enCursoPorConductor = viajes.stream()
                .filter(v -> v.getEstado() == EstadoViaje.EN_CURSO)
                .collect(Collectors.toMap(Viaje::getConductorId, v -> v, (a, b) -> a));

        Map<UUID, UUID> alertaAbiertaPorViaje = alertaRepository.listarPorFlotaYAtendida(flotaId, false)
                .stream()
                .collect(Collectors.toMap(AlertaCritica::getViajeId, AlertaCritica::getId, (a, b) -> a));

        List<ConductorEnViaje> enViaje = new ArrayList<>();
        List<ConductorSinViaje> sinViaje = new ArrayList<>();
        Map<EstadoMonitoreo, Integer> conteo = new EnumMap<>(EstadoMonitoreo.class);
        for (EstadoMonitoreo e : EstadoMonitoreo.values()) {
            conteo.put(e, 0);
        }

        for (PerfilConductor perfil : porId.values()) {
            Viaje activo = enCursoPorConductor.get(perfil.getUsuarioId());
            ConductorResumen resumen = ConductorResumen.desde(perfil);
            if (activo != null) {
                EstadoMonitoreo estado = activo.calcularEstadoMonitoreo(ahora, activo.getUltimaSenalEn());
                conteo.merge(estado, 1, Integer::sum);
                Ubicacion u = activo.getUltimaUbicacion();
                enViaje.add(new ConductorEnViaje(
                        resumen,
                        activo.getId(),
                        estado,
                        activo.getNivelAlerta(),
                        activo.getFechaInicio(),
                        activo.getUltimaSenalEn(),
                        u != null ? u.latitud() : null,
                        u != null ? u.longitud() : null,
                        activo.getVelocidadKmh(),
                        alertaAbiertaPorViaje.get(activo.getId())));
            } else {
                conteo.merge(EstadoMonitoreo.SIN_VIAJE, 1, Integer::sum);
                sinViaje.add(new ConductorSinViaje(resumen,
                        fechaUltimoViaje(perfil.getUsuarioId(), viajes)));
            }
        }

        enViaje.sort(Comparator.comparingInt(c -> gravedad(c.estado())));
        sinViaje.sort(Comparator.comparing(ConductorSinViaje::fechaUltimoViaje,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return new EstadoFlota(ahora, conteo, enViaje, sinViaje);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaVista> listarAlertasAbiertas(UUID flotaId) {
        return alertaRepository.listarPorFlotaYAtendida(flotaId, false).stream()
                .sorted(Comparator.comparing(AlertaCritica::getFechaApertura,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> new AlertaVista(a, resumenConductor(a.getConductorId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaDetalleVista obtenerAlerta(UUID flotaId, UUID alertaId) {
        AlertaCritica alerta = cargarEnFlota(flotaId, alertaId);
        Viaje viaje = viajeRepository.buscarPorId(alerta.getViajeId()).orElse(null);
        LocalDateTime inicioViaje = viaje != null ? viaje.getFechaInicio() : null;
        LocalDateTime ultimaSenal = viaje != null ? viaje.getUltimaSenalEn() : null;

        return new AlertaDetalleVista(
                alerta,
                resumenConductor(alerta.getConductorId()),
                inicioViaje,
                ultimaSenal,
                construirLineaTiempo(alerta));
    }

    @Override
    @Transactional
    public AlertaVista atenderAlerta(UUID flotaId, UUID despachadorId, String despachadorNombre,
                                     UUID alertaId, String notas) {
        AlertaCritica alerta = cargarEnFlota(flotaId, alertaId);
        String nombre = usuarioRepository.buscarPorId(despachadorId)
                .map(com.unal.tire.domain.model.Usuario::getNombre)
                .orElse(despachadorNombre);
        alerta.atender(despachadorId, nombre, notas);
        AlertaCritica guardada = alertaRepository.guardar(alerta);
        return new AlertaVista(guardada, resumenConductor(guardada.getConductorId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaEventos historialEventos(UUID flotaId, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                          TipoEvento tipoEvento, UUID conductorId, int page, int size) {
        if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw new ReglaNegocioException("La fecha final no puede ser anterior a la inicial.");
        }

        List<PerfilConductor> conductores = conductorRepository.listarPorFlota(flotaId);
        Map<UUID, PerfilConductor> porId = conductores.stream()
                .collect(Collectors.toMap(PerfilConductor::getUsuarioId, p -> p));

        // Alerta por evento para calcular el estado de atencion.
        Map<UUID, AlertaCritica> alertaPorEvento = alertaRepository.listarPorFlota(flotaId).stream()
                .filter(a -> a.getEventoId() != null)
                .collect(Collectors.toMap(AlertaCritica::getEventoId, a -> a, (a, b) -> a));

        List<EventoHistorial> todos = new ArrayList<>();
        for (Viaje viaje : viajeRepository.listarPorConductores(new ArrayList<>(porId.keySet()))) {
            PerfilConductor perfil = porId.get(viaje.getConductorId());
            if (perfil == null) {
                continue;
            }
            if (conductorId != null && !conductorId.equals(viaje.getConductorId())) {
                continue;
            }
            ConductorResumen resumen = ConductorResumen.desde(perfil);
            for (EventoFatiga evento : viaje.getEventosFatiga()) {
                if (tipoEvento != null && evento.getTipoEvento() != tipoEvento) {
                    continue;
                }
                LocalDateTime fecha = evento.getFechaHora();
                if (fechaInicio != null && fecha != null && fecha.isBefore(fechaInicio)) {
                    continue;
                }
                if (fechaFin != null && fecha != null && fecha.isAfter(fechaFin)) {
                    continue;
                }
                todos.add(new EventoHistorial(evento, resumen,
                        calcularAtencion(evento, alertaPorEvento.get(evento.getId()))));
            }
        }

        todos.sort(Comparator.comparing(
                (EventoHistorial h) -> h.evento().getFechaHora(),
                Comparator.nullsLast(Comparator.reverseOrder())));

        long totalElementos = todos.size();
        int totalPaginas = size > 0 ? (int) Math.ceil((double) totalElementos / size) : 0;
        int desde = Math.min(page * size, todos.size());
        int hasta = Math.min(desde + size, todos.size());
        List<EventoHistorial> contenido = todos.subList(desde, hasta);

        return new PaginaEventos(page, size, totalElementos, totalPaginas, contenido);
    }

    // ----- Helpers -----

    private AlertaCritica cargarEnFlota(UUID flotaId, UUID alertaId) {
        AlertaCritica alerta = alertaRepository.buscarPorId(alertaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada"));
        if (flotaId == null || !flotaId.equals(alerta.getFlotaId())) {
            throw new RecursoNoEncontradoException("Alerta no encontrada");
        }
        return alerta;
    }

    private ConductorResumen resumenConductor(UUID conductorId) {
        return conductorRepository.buscarPorId(conductorId)
                .map(ConductorResumen::desde)
                .orElse(new ConductorResumen(conductorId, "", null, null));
    }

    private LocalDateTime fechaUltimoViaje(UUID conductorId, List<Viaje> viajes) {
        return viajes.stream()
                .filter(v -> v.getConductorId().equals(conductorId))
                .filter(v -> v.getEstado() == EstadoViaje.FINALIZADO)
                .map(Viaje::getFechaFin)
                .filter(f -> f != null)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    private String calcularAtencion(EventoFatiga evento, AlertaCritica alerta) {
        if (alerta == null) {
            return "NO_REQUIERE";
        }
        return alerta.isAtendida() ? "ATENDIDA" : "SIN_ATENDER";
    }

    /** Orden de gravedad: CRITICO, SIN_SENAL, PRECAUCION, NORMAL. */
    private int gravedad(EstadoMonitoreo estado) {
        return switch (estado) {
            case CRITICO -> 0;
            case SIN_SENAL -> 1;
            case PRECAUCION -> 2;
            case NORMAL -> 3;
            case SIN_VIAJE -> 4;
        };
    }

    /**
     * Arma la linea de tiempo de una alerta a partir de las fechas conocidas del
     * evento y de la alerta.
     */
    private List<EntradaLineaTiempo> construirLineaTiempo(AlertaCritica alerta) {
        List<EntradaLineaTiempo> entradas = new ArrayList<>();
        LocalDateTime fechaEvento = alerta.getFechaEvento();
        if (fechaEvento != null) {
            entradas.add(new EntradaLineaTiempo(fechaEvento, "DETECTADO",
                    "Microsueño detectado"));
            entradas.add(new EntradaLineaTiempo(fechaEvento, "ALARMA_ACTIVADA",
                    "Alarma sonora activada en la app"));
            entradas.add(new EntradaLineaTiempo(fechaEvento.plusSeconds(5), "ALARMA_INTENSIFICADA",
                    "Sin respuesta en 5 s, alarma intensificada"));
        }
        if (alerta.getFechaApertura() != null) {
            entradas.add(new EntradaLineaTiempo(alerta.getFechaApertura(), "ALERTA_ENVIADA",
                    "Alerta enviada al panel del despachador"));
        }
        if (alerta.isAtendida() && alerta.getFechaAtencion() != null) {
            String desc = alerta.getAtendidaPorNombre() != null
                    ? "Atendida por " + alerta.getAtendidaPorNombre()
                    : "Alerta atendida";
            entradas.add(new EntradaLineaTiempo(alerta.getFechaAtencion(), "ATENDIDA", desc));
        }
        entradas.sort(Comparator.comparing(EntradaLineaTiempo::fechaHora,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return entradas;
    }
}
