package com.unal.tire.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa a un conductor de la flota.
 *
 * <p>Java puro: sin dependencias de Spring, JPA ni Jackson.</p>
 */
public class Conductor {

    private UUID id;
    private String nombre;
    private String documento;
    private String correo;
    private String telefono;
    private UUID flotaId;
    private boolean activo;
    private LocalDateTime creadoEn;

    public Conductor() {
    }

    public Conductor(UUID id, String nombre, String documento, String correo,
                     String telefono, UUID flotaId, boolean activo, LocalDateTime creadoEn) {
        this.id = id;
        this.nombre = nombre;
        this.documento = documento;
        this.correo = correo;
        this.telefono = telefono;
        this.flotaId = flotaId;
        this.activo = activo;
        this.creadoEn = creadoEn;
    }

    /**
     * Soft delete: desactiva al conductor sin borrar su historial.
     */
    public void desactivar() {
        this.activo = false;
    }

    /**
     * Actualiza los datos editables del conductor. Los parametros nulos se
     * ignoran (no se sobreescriben los valores existentes).
     *
     * @param nombre   nuevo nombre, o {@code null} para conservar el actual.
     * @param telefono nuevo telefono, o {@code null} para conservar el actual.
     * @param activo   nuevo estado, o {@code null} para conservar el actual.
     */
    public void actualizarDatos(String nombre, String telefono, Boolean activo) {
        if (nombre != null) {
            this.nombre = nombre;
        }
        if (telefono != null) {
            this.telefono = telefono;
        }
        if (activo != null) {
            this.activo = activo;
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
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
        if (!(o instanceof Conductor conductor)) {
            return false;
        }
        return Objects.equals(id, conductor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
