package com.unal.tire.application.service;

import com.unal.tire.application.port.in.AutenticacionUseCase;
import com.unal.tire.application.port.in.ComandoRegistro;
import com.unal.tire.application.port.in.RespuestaToken;
import com.unal.tire.application.port.out.ProveedorAutenticacionPort;
import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.exception.ReglaNegocioException;
import com.unal.tire.domain.model.Usuario;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de autenticacion: registro, inicio de sesion y cambio de
 * contrasena.
 *
 * <p>Orquesta los puertos de salida ({@link UsuarioRepositoryPort} y
 * {@link ProveedorAutenticacionPort}) sin conocer su implementacion concreta.</p>
 */
@Service
public class AutenticacionService implements AutenticacionUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final ProveedorAutenticacionPort proveedorAutenticacion;
    private final long expiracionMs;

    public AutenticacionService(
            UsuarioRepositoryPort usuarioRepository,
            ProveedorAutenticacionPort proveedorAutenticacion,
            @Value("${jwt.expiration-ms}") long expiracionMs) {
        this.usuarioRepository = usuarioRepository;
        this.proveedorAutenticacion = proveedorAutenticacion;
        this.expiracionMs = expiracionMs;
    }

    @Override
    @Transactional
    public Usuario registrarUsuario(ComandoRegistro comando) {
        if (usuarioRepository.existePorEmail(comando.email())) {
            throw new ConflictoEstadoException("El correo electrónico ya está registrado");
        }

        String passwordHash = proveedorAutenticacion.hashearContrasena(comando.password());

        Usuario usuario = new Usuario(
                UUID.randomUUID(),
                comando.email(),
                passwordHash,
                comando.nombre(),
                comando.rol(),
                comando.flotaId(),
                true);

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

        String accessToken = proveedorAutenticacion.emitirToken(usuario);

        return new RespuestaToken(
                accessToken,
                "Bearer",
                expiracionMs / 1000,
                usuario.getRol(),
                usuario.getId(),
                usuario.getNombre());
    }

    @Override
    @Transactional
    public void cambiarContrasena(UUID usuarioId, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (!proveedorAutenticacion.verificarContrasena(passwordActual, usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        if (passwordNueva == null || passwordNueva.length() < 8) {
            throw new ReglaNegocioException("La nueva contraseña debe tener al menos 8 caracteres.");
        }

        usuario.setPasswordHash(proveedorAutenticacion.hashearContrasena(passwordNueva));
        usuarioRepository.guardar(usuario);
    }
}
