package com.unal.tire.infrastructure.adapter.out.persistence;

import com.unal.tire.domain.valueobject.RolUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Entidad JPA que mapea la tabla {@code usuarios}.
 *
 * <p>Vive en la capa de infraestructura; nunca se filtra al dominio.</p>
 */
@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private RolUsuario rol;

    @Column(name = "flota_id")
    private UUID flotaId;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    protected UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(UUID id, String email, String passwordHash, String nombre,
                            RolUsuario rol, UUID flotaId, boolean activo) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nombre = nombre;
        this.rol = rol;
        this.flotaId = flotaId;
        this.activo = activo;
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
}
