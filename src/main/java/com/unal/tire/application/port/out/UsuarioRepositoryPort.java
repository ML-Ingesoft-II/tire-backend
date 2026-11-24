package com.unal.tire.application.port.out;

import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.valueobject.RolUsuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de usuarios.
 *
 * <p>Define el contrato que la capa de aplicacion necesita de la
 * infraestructura, sin acoplarse a ninguna tecnologia concreta.</p>
 */
public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario u);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(UUID id);

    boolean existePorEmail(String email);

    /**
     * Usuarios de una flota con un rol dado (p. ej. todos los CONDUCTOR de la
     * flota del despachador).
     */
    List<Usuario> listarPorFlotaYRol(UUID flotaId, RolUsuario rol);
}
