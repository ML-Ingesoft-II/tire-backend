package com.unal.tire.application.service;

import com.unal.tire.application.port.in.AutenticacionUseCase;
import com.unal.tire.application.port.in.ComandoRegistro;
import com.unal.tire.application.port.in.RespuestaToken;
import com.unal.tire.application.port.out.FlotaRepositoryPort;
import com.unal.tire.application.port.out.ProveedorAutenticacionPort;
import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.exception.ReglaNegocioException;
import com.unal.tire.domain.exception.TokenRecuperacionInvalidoException;
import com.unal.tire.domain.model.Flota;
import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.valueobject.RolUsuario;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de autenticacion: registro de conductor particular, inicio de
 * sesion, renovacion de sesion, cambio y recuperacion de contrasena.
 *
 * <p>Orquesta los puertos de salida sin conocer su implementacion concreta.</p>
 */
@Service
public class AutenticacionService implements AutenticacionUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final ProveedorAutenticacionPort proveedorAutenticacion;
    private final FlotaRepositoryPort flotaRepository;
    private final long expiracionMs;

    /**
     * Tokens de recuperacion de un solo uso (en memoria, suficiente para el
     * prototipo). Mapea token -> (usuarioId, vencimiento).
     */
    private final Map<String, TokenRecuperacion> tokensRecuperacion = new ConcurrentHashMap<>();

    public AutenticacionService(
            UsuarioRepositoryPort usuarioRepository,
            ProveedorAutenticacionPort proveedorAutenticacion,
            FlotaRepositoryPort flotaRepository,
            @Value("${jwt.expiration-ms}") long expiracionMs) {
        this.usuarioRepository = usuarioRepository;
        this.proveedorAutenticacion = proveedorAutenticacion;
        this.flotaRepository = flotaRepository;
        this.expiracionMs = expiracionMs;
    }

    @Override
    @Transactional
    public Usuario registrarConductorParticular(ComandoRegistro comando) {
        if (usuarioRepository.existePorEmail(comando.email())) {
            throw new ConflictoEstadoException("Este correo ya tiene una cuenta");
        }

        String passwordHash = proveedorAutenticacion.hashearContrasena(comando.password());

        // Conductor particular: rol CONDUCTOR, sin flota, sin obligacion de
        // cambiar contrasena (eligio la suya al registrarse).
        Usuario usuario = new Usuario(
                UUID.randomUUID(),
                comando.email(),
                passwordHash,
                comando.nombre(),
                RolUsuario.CONDUCTOR,
                null,
                true,
                false);

        return usuarioRepository.guardar(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public RespuestaToken iniciarSesion(String email, String password) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!usuario.isActivo()
                || !proveedorAutenticacion.verificarContrasena(password, usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return construirRespuesta(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public RespuestaToken renovarSesion(String refreshToken) {
        UUID usuarioId;
        try {
            usuarioId = proveedorAutenticacion.validarRefreshToken(refreshToken);
        } catch (RuntimeException ex) {
            throw new BadCredentialsException("Refresh token inválido o vencido");
        }

        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new BadCredentialsException("Refresh token inválido o vencido"));

        if (!usuario.isActivo()) {
            throw new BadCredentialsException("Cuenta desactivada");
        }

        return construirRespuesta(usuario);
    }

    private RespuestaToken construirRespuesta(Usuario usuario) {
        String accessToken = proveedorAutenticacion.emitirToken(usuario);
        String refreshToken = proveedorAutenticacion.emitirRefreshToken(usuario);

        String nombreFlota = null;
        if (usuario.getFlotaId() != null) {
            nombreFlota = flotaRepository.buscarPorId(usuario.getFlotaId())
                    .map(Flota::getNombre)
                    .orElse(null);
        }

        return new RespuestaToken(
                accessToken,
                refreshToken,
                "Bearer",
                expiracionMs / 1000,
                usuario.getRol(),
                usuario.getId(),
                usuario.getNombre(),
                usuario.getFlotaId(),
                nombreFlota,
                usuario.isDebeCambiarPassword());
    }

    @Override
    @Transactional
    public void cambiarContrasena(UUID usuarioId, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!proveedorAutenticacion.verificarContrasena(passwordActual, usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        validarPasswordNueva(passwordNueva, passwordActual);

        usuario.cambiarPassword(proveedorAutenticacion.hashearContrasena(passwordNueva));
        usuarioRepository.guardar(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public void solicitarRecuperacion(String email) {
        // La respuesta es la misma exista o no el correo (no revelar cuentas).
        usuarioRepository.buscarPorEmail(email).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            tokensRecuperacion.put(token, new TokenRecuperacion(
                    usuario.getId(), LocalDateTime.now().plusHours(1)));
            // En produccion aqui se enviaria el enlace por correo.
        });
    }

    @Override
    @Transactional
    public void confirmarRecuperacion(String token, String passwordNueva) {
        TokenRecuperacion datos = tokensRecuperacion.get(token);
        if (datos == null || datos.vencimiento().isBefore(LocalDateTime.now())) {
            tokensRecuperacion.remove(token);
            throw new TokenRecuperacionInvalidoException("El enlace venció. Solicita uno nuevo.");
        }

        Usuario usuario = usuarioRepository.buscarPorId(datos.usuarioId())
                .orElseThrow(() -> new TokenRecuperacionInvalidoException(
                        "El enlace venció. Solicita uno nuevo."));

        validarPasswordNueva(passwordNueva, null);

        usuario.cambiarPassword(proveedorAutenticacion.hashearContrasena(passwordNueva));
        usuarioRepository.guardar(usuario);

        // Token de un solo uso: se invalida tras usarse.
        tokensRecuperacion.remove(token);
    }

    private void validarPasswordNueva(String passwordNueva, String passwordActual) {
        if (passwordNueva == null || passwordNueva.length() < 8) {
            throw new ReglaNegocioException("La nueva contraseña debe tener al menos 8 caracteres.");
        }
        if (passwordActual != null && passwordActual.equals(passwordNueva)) {
            throw new ReglaNegocioException("La nueva contraseña debe ser distinta de la actual.");
        }
    }

    private record TokenRecuperacion(UUID usuarioId, LocalDateTime vencimiento) {
    }
}
