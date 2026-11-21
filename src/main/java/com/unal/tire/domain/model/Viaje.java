package com.unal.tire.domain.model;

import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.ReglaNegocioException;
import com.unal.tire.domain.state.EstadoAlertaState;
import com.unal.tire.domain.state.FabricaEstadoAlerta;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.domain.valueobject.EstadoMonitoreo;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado raiz que modela un viaje y protege sus invariantes de negocio.
 *
 * <p>El nivel de alerta se gobierna mediante el patron GoF State
 * ({@link EstadoAlertaState}), rehidratado desde el {@link NivelAlerta}
 * persistido mediante {@link FabricaEstadoAlerta}.</p>
 *
 * <p>Java puro: sin dependencias de Spring, JPA ni Jackson.</p>
 */
public class Viaje {

    private UUID id;
    private UUID conductorId;
    private String origen;
    private String destino;
    private EstadoViaje estado;
    private NivelAlerta nivelAlerta;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Ubicacion ultimaUbicacion;
    private CondicionClimatica ultimoClima;
    private LocalDateTime ultimaSenalEn;
    private Double velocidadKmh;
    private List<EventoFatiga> eventosFatiga;

    public Viaje() {
        this.eventosFatiga = new ArrayList<>();
        this.estado = EstadoViaje.EN_CURSO;
        this.nivelAlerta = NivelAlerta.NORMAL;
    }

    public Viaje(UUID id, UUID conductorId, String origen, String destino, EstadoViaje estado,
                 NivelAlerta nivelAlerta, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                 Ubicacion ultimaUbicacion, CondicionClimatica ultimoClima,
                 LocalDateTime ultimaSenalEn, Double velocidadKmh,
                 List<EventoFatiga> eventosFatiga) {
        this.id = id;
        this.conductorId = conductorId;
        this.origen = origen;
        this.destino = destino;
        this.estado = estado != null ? estado : EstadoViaje.EN_CURSO;
        this.nivelAlerta = nivelAlerta != null ? nivelAlerta : NivelAlerta.NORMAL;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.ultimaUbicacion = ultimaUbicacion;
        this.ultimoClima = ultimoClima;
        this.ultimaSenalEn = ultimaSenalEn;
        this.velocidadKmh = velocidadKmh;
        this.eventosFatiga = eventosFatiga != null ? new ArrayList<>(eventosFatiga) : new ArrayList<>();
    }

    /**
     * Registra telemetria (posicion y clima) del viaje en curso.
     *
     * @throws ConflictoEstadoException si el viaje no esta en curso.
     */
    public void registrarTelemetria(Ubicacion ubicacion, Double velocidadKmh,
                                    CondicionClimatica clima, LocalDateTime momento) {
        exigirEnCurso("Un viaje finalizado o cancelado no acepta nueva telemetría");
        if (ubicacion != null) {
            this.ultimaUbicacion = ubicacion;
        }
        if (velocidadKmh != null) {
            this.velocidadKmh = velocidadKmh;
        }
        if (clima != null) {
            this.ultimoClima = clima;
        }
        this.ultimaSenalEn = momento != null ? momento : LocalDateTime.now();
    }

    /**
     * Registra un evento de fatiga y recalcula el nivel de alerta aplicando
     * el patron State.
     *
     * @throws ConflictoEstadoException si el viaje no esta en curso.
     */
    public void registrarEventoFatiga(EventoFatiga evento) {
        exigirEnCurso("No se puede registrar un evento de fatiga en un viaje que no está en curso.");
        if (evento == null) {
            throw new ReglaNegocioException("El evento de fatiga es obligatorio.");
        }

        this.eventosFatiga.add(evento);

        // Solo los microsuenos recalculan el nivel de alerta (patron State).
        // El evento de rostro no visible queda en el historial sin afectar el nivel.
        if (evento.afectaNivelAlerta()) {
            long microsuenosRecientes = this.eventosFatiga.stream()
                    .filter(EventoFatiga::afectaNivelAlerta)
                    .count();
            EstadoAlertaState estadoActual = FabricaEstadoAlerta.desdeNivel(this.nivelAlerta);
            EstadoAlertaState nuevoEstado = estadoActual.registrarMicrosueno(
                    evento.fueConfirmado(), (int) microsuenosRecientes);
            this.nivelAlerta = nuevoEstado.obtenerNivel();
        }

        if (evento.getUbicacion() != null) {
            this.ultimaUbicacion = evento.getUbicacion();
        }
    }

    /**
     * El conductor confirma estar atento; recalcula el nivel de alerta.
     *
     * @throws ConflictoEstadoException si el viaje no esta en curso.
     */
    public void confirmarAlertaConductor() {
        exigirEnCurso("No se puede confirmar la alerta de un viaje que no está en curso.");
        EstadoAlertaState estadoActual = FabricaEstadoAlerta.desdeNivel(this.nivelAlerta);
        EstadoAlertaState nuevoEstado = estadoActual.confirmarAlerta();
        this.nivelAlerta = nuevoEstado.obtenerNivel();
    }

    /**
     * Finaliza el viaje.
     *
     * @throws ConflictoEstadoException si el viaje no esta en curso.
     * @throws ReglaNegocioException    si {@code fechaFin} es anterior al inicio.
     */
    public void finalizar(LocalDateTime fechaFin) {
        exigirEnCurso("No se puede finalizar un viaje que no está en curso.");
        if (fechaFin == null) {
            throw new ReglaNegocioException("La fecha de fin es obligatoria.");
        }
        if (fechaInicio != null && fechaFin.isBefore(fechaInicio)) {
            throw new ReglaNegocioException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        this.estado = EstadoViaje.FINALIZADO;
        this.fechaFin = fechaFin;
    }

    /**
     * Cancela el viaje. Solo es posible si esta en curso y no tiene eventos
     * de fatiga registrados.
     *
     * @throws ConflictoEstadoException si no esta en curso o ya tiene eventos.
     */
    public void cancelar() {
        if (estado != EstadoViaje.EN_CURSO || !eventosFatiga.isEmpty()) {
            throw new ConflictoEstadoException(
                    "No se puede cancelar un viaje que ya tiene eventos de fatiga registrados o que ya terminó");
        }
        this.estado = EstadoViaje.CANCELADO;
    }

    /** Minutos sin telemetria tras los cuales el panel marca "Sin señal". */
    public static final long UMBRAL_SIN_SENAL_MINUTOS = 2;

    /**
     * Calcula el estado que el panel del despachador debe mostrar para este
     * viaje en curso (HU-23). Si el viaje no esta en curso devuelve el nivel
     * de alerta tal cual.
     *
     * @param ahora          instante de referencia.
     * @param ultimaSenalEn  fecha del ultimo reporte de telemetria (o inicio).
     */
    public EstadoMonitoreo calcularEstadoMonitoreo(LocalDateTime ahora, LocalDateTime ultimaSenalEn) {
        if (estado == EstadoViaje.EN_CURSO && ultimaSenalEn != null
                && ultimaSenalEn.isBefore(ahora.minusMinutes(UMBRAL_SIN_SENAL_MINUTOS))) {
            return EstadoMonitoreo.SIN_SENAL;
        }
        return switch (nivelAlerta) {
            case NORMAL -> EstadoMonitoreo.NORMAL;
            case PRECAUCION -> EstadoMonitoreo.PRECAUCION;
            case CRITICO -> EstadoMonitoreo.CRITICO;
        };
    }

    private void exigirEnCurso(String mensaje) {
        if (estado != EstadoViaje.EN_CURSO) {
            throw new ConflictoEstadoException(mensaje);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getConductorId() {
        return conductorId;
    }

    public void setConductorId(UUID conductorId) {
        this.conductorId = conductorId;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public EstadoViaje getEstado() {
        return estado;
    }

    public void setEstado(EstadoViaje estado) {
        this.estado = estado;
    }

    public NivelAlerta getNivelAlerta() {
        return nivelAlerta;
    }

    public void setNivelAlerta(NivelAlerta nivelAlerta) {
        this.nivelAlerta = nivelAlerta;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Ubicacion getUltimaUbicacion() {
        return ultimaUbicacion;
    }

    public void setUltimaUbicacion(Ubicacion ultimaUbicacion) {
        this.ultimaUbicacion = ultimaUbicacion;
    }

    public CondicionClimatica getUltimoClima() {
        return ultimoClima;
    }

    public void setUltimoClima(CondicionClimatica ultimoClima) {
        this.ultimoClima = ultimoClima;
    }

    public LocalDateTime getUltimaSenalEn() {
        return ultimaSenalEn;
    }

    public void setUltimaSenalEn(LocalDateTime ultimaSenalEn) {
        this.ultimaSenalEn = ultimaSenalEn;
    }

    public Double getVelocidadKmh() {
        return velocidadKmh;
    }

    public void setVelocidadKmh(Double velocidadKmh) {
        this.velocidadKmh = velocidadKmh;
    }

    /**
     * @return vista inmutable de los eventos de fatiga del viaje.
     */
    public List<EventoFatiga> getEventosFatiga() {
        return Collections.unmodifiableList(eventosFatiga);
    }

    public void setEventosFatiga(List<EventoFatiga> eventosFatiga) {
        this.eventosFatiga = eventosFatiga != null ? new ArrayList<>(eventosFatiga) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Viaje viaje)) {
            return false;
        }
        return Objects.equals(id, viaje.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
