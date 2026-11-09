package com.unal.tire.application.port.out;

import com.unal.tire.domain.model.Usuario;

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

    ClaimsAutenticacion validarToken(String token);
}
