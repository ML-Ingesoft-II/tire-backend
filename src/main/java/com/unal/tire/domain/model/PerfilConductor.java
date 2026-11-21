package com.unal.tire.domain.model;

import com.unal.tire.domain.valueobject.TipoDocumento;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Perfil operativo de un conductor.
 *
 * <p>Complementa al {@link Usuario} (la credencial de acceso con rol
 * {@code CONDUCTOR}) con los datos de negocio: nombres, documento, placa y
 * telefono. El {@code usuarioId} de este perfil <b>es el mismo UUID</b> del
 * usuario, de su cuenta y de sus viajes; por eso actua como identidad unica
 * del conductor en todo el sistema.</p>
 *
 * <p>Java puro: sin dependencias de Spring, JPA ni Jackson.</p>
 */
public class PerfilConductor {

    /** Identidad del conductor: coincide con el id del {@link Usuario}. */
    private UUID usuarioId;
    private String nombres;
    private String apellidos;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String telefono;
    private String placaVehiculo;
    private UUID flotaId;
    private boolean activo;
    private LocalDateTime creadoEn;

    public PerfilConductor() {
    }

    public PerfilConductor(UUID usuarioId, String nombres, String apellidos,
                           TipoDocumento tipoDocumento, String numeroDocumento,
                           String telefono, String placaVehiculo, UUID flotaId,
                           boolean activo, LocalDateTime creadoEn) {
        this.usuarioId = usuarioId;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.telefono = telefono;
        this.placaVehiculo = placaVehiculo;
        this.flotaId = flotaId;
        this.activo = activo;
        this.creadoEn = creadoEn;
    }

    /**
     * Nombre completo para mostrar en el panel ("nombres apellidos").
     */
    public String getNombreCompleto() {
        String n = nombres != null ? nombres.trim() : "";
        String a = apellidos != null ? apellidos.trim() : "";
        return (n + " " + a).trim();
    }

    /**
     * Soft delete: desactiva al conductor sin borrar su historial.
     */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Actualiza los datos editables del perfil. Los parametros nulos se
     * ignoran (no se sobreescriben los valores existentes).
     */
    public void actualizarDatos(String nombres, String apellidos, TipoDocumento tipoDocumento,
                                String numeroDocumento, String telefono, String placaVehiculo,
                                Boolean activo) {
        if (nombres != null) {
            this.nombres = nombres;
        }
        if (apellidos != null) {
            this.apellidos = apellidos;
        }
        if (tipoDocumento != null) {
            this.tipoDocumento = tipoDocumento;
        }
        if (numeroDocumento != null) {
            this.numeroDocumento = numeroDocumento;
        }
        if (telefono != null) {
            this.telefono = telefono;
        }
        if (placaVehiculo != null) {
            this.placaVehiculo = placaVehiculo;
        }
        if (activo != null) {
            this.activo = activo;
        }
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPlacaVehiculo() {
        return placaVehiculo;
    }

    public void setPlacaVehiculo(String placaVehiculo) {
        this.placaVehiculo = placaVehiculo;
    }

    public UUID getFlotaId() {
        return flotaId;
    }

    public void setFlotaId(UUID flotaId) {
        this.flotaId = flotaId;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PerfilConductor that)) {
            return false;
        }
        return Objects.equals(usuarioId, that.usuarioId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId);
    }
}
