package com.unal.tire.application.port.in;

import com.unal.tire.domain.model.Usuario;
import java.util.UUID;

/**
 * Puerto de entrada del caso de uso de autenticacion y gestion de acceso.
 */
public interface AutenticacionUseCase {

    Usuario registrarUsuario(ComandoRegistro comando);

    RespuestaToken iniciarSesion(String email, String password);

    void cambiarContrasena(UUID usuarioId, String passwordActual, String passwordNueva);

    void solicitarRecuperacion(String email);
}
