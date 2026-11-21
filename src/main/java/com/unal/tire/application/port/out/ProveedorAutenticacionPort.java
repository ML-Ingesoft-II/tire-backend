package com.unal.tire.application.port.out;

import com.unal.tire.domain.model.Usuario;
import java.util.UUID;

/**
 * Puerto de salida (Target del patron GoF <b>Adapter</b>) que abstrae el
 * proveedor de autenticacion: hash de contrasenas y emision/validacion de
 * tokens.
 *
 * <p>La capa de aplicacion depende de este contrato, no de BCrypt ni de
 * la libreria JWT concreta.</p>
 */
public interface ProveedorAutenticacionPort {

    String hashearContrasena(String passwordPlano);

    boolean verificarContrasena(String passwordPlano, String passwordHash);

    String emitirToken(Usuario usuario);

    /**
     * Emite un refresh token de larga duracion asociado al usuario.
     */
    String emitirRefreshToken(Usuario usuario);

    ClaimsAutenticacion validarToken(String token);

    /**
     * Valida un refresh token y devuelve el id del usuario al que pertenece.
     *
     * @throws RuntimeException si el token no es un refresh token valido.
     */
    UUID validarRefreshToken(String refreshToken);
}
