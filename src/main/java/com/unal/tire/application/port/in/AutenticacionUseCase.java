package com.unal.tire.application.port.in;

import com.unal.tire.domain.model.Usuario;
import java.util.UUID;

/**
 * Puerto de entrada del caso de uso de autenticacion y gestion de acceso.
 */
public interface AutenticacionUseCase {

    /**
     * Registra un conductor particular (rol CONDUCTOR, sin flota), ruta publica
     * del contrato ({@code POST /auth/registro}).
     */
    Usuario registrarConductorParticular(ComandoRegistro comando);

    RespuestaToken iniciarSesion(String email, String password);

    /** Renueva el par de tokens a partir de un refresh token valido. */
    RespuestaToken renovarSesion(String refreshToken);

    void cambiarContrasena(UUID usuarioId, String passwordActual, String passwordNueva);

    void solicitarRecuperacion(String email);

    /** Define una nueva contrasena usando un token de recuperacion de un solo uso. */
    void confirmarRecuperacion(String token, String passwordNueva);
}
