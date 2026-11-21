package com.unal.tire.domain.model;

import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad de dominio que representa una credencial de acceso al sistema.
 *
 * <p>Java puro: sin dependencias de Spring, JPA ni Jackson.</p>
 */
public class Usuario {

    private UUID id;
    private String email;
    private String passwordHash;
    private String nombre;
    private RolUsuario rol;
    private UUID flotaId;
    private boolean activo;
    private boolean debeCambiarPassword;

    public Usuario() {
    }

    public Usuario(UUID id, String email, String passwordHash, String nombre,
                   RolUsuario rol, UUID flotaId, boolean activo) {
        this(id, email, passwordHash, nombre, rol, flotaId, activo, false);
    }

    public Usuario(UUID id, String email, String passwordHash, String nombre,
                   RolUsuario rol, UUID flotaId, boolean activo, boolean debeCambiarPassword) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.rol = rol;
        this.flotaId = flotaId;
        this.activo = activo;
        this.debeCambiarPassword = debeCambiarPassword;
    }

    /**
     * Cambia la contrasena y marca que ya no usa la contrasena inicial.
     */
    public void cambiarPassword(String nuevoHash) {
        this.passwordHash = nuevoHash;
        this.debeCambiarPassword = false;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
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

    public boolean isDebeCambiarPassword() {
        return debeCambiarPassword;
    }

    public void setDebeCambiarPassword(boolean debeCambiarPassword) {
        this.debeCambiarPassword = debeCambiarPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario usuario)) {
            return false;
        }
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
