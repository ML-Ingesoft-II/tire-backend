package com.unal.tire.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private ProveedorAutenticacionPort proveedorAutenticacion;

    @Mock
    private FlotaRepositoryPort flotaRepository;

    private AutenticacionService service;

    private final long expiracionMs = 3600000L;
    private final UUID usuarioId = UUID.randomUUID();
    private final UUID flotaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AutenticacionService(
                usuarioRepository,
                proveedorAutenticacion,
                flotaRepository,
                expiracionMs
        );
    }

    @Test
    void registrarConductorParticular_datosValidos_guardaUsuarioYRetorna() {
        // Arrange
        ComandoRegistro comando = new ComandoRegistro("Pedro Gómez", "pedro@gmail.com", "Password123");
        when(usuarioRepository.existePorEmail(comando.email())).thenReturn(false);
        when(proveedorAutenticacion.hashearContrasena(comando.password())).thenReturn("hashed123");
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Usuario resultado = service.registrarConductorParticular(comando);

        // Assert
        assertNotNull(resultado);
        assertEquals("Pedro Gómez", resultado.getNombre());
        assertEquals("pedro@gmail.com", resultado.getEmail());
        assertEquals("hashed123", resultado.getPasswordHash());
        assertEquals(RolUsuario.CONDUCTOR, resultado.getRol());
        assertTrue(resultado.isActivo());
        assertFalse(resultado.isDebeCambiarPassword());

        verify(usuarioRepository, times(1)).guardar(any(Usuario.class));
    }

    @Test
    void registrarConductorParticular_correoDuplicado_lanzaConflictoYSinGuardar() {
        // Arrange
        ComandoRegistro comando = new ComandoRegistro("Pedro Gómez", "pedro@gmail.com", "Password123");
        when(usuarioRepository.existePorEmail(comando.email())).thenReturn(true);

        // Act & Assert
        ConflictoEstadoException ex = assertThrows(ConflictoEstadoException.class,
                () -> service.registrarConductorParticular(comando));
        assertEquals("Este correo ya tiene una cuenta", ex.getMessage());

        verify(usuarioRepository, never()).guardar(any());
        verify(proveedorAutenticacion, never()).hashearContrasena(any());
    }

    @Test
    void iniciarSesion_credencialesValidasConFlota_emiteParDeTokensYRetornaRespuesta() {
        // Arrange
        Usuario usuario = new Usuario(
                usuarioId, "laura@tandinos.co", "hashed123", "Laura Gómez",
                RolUsuario.DESPACHADOR, flotaId, true, false
        );
        Flota flota = new Flota(flotaId, "Transportes Andinos");

        when(usuarioRepository.buscarPorEmail("laura@tandinos.co")).thenReturn(Optional.of(usuario));
        when(proveedorAutenticacion.verificarContrasena("Password123", "hashed123")).thenReturn(true);
        when(proveedorAutenticacion.emitirToken(usuario)).thenReturn("access_token_jwt");
        when(proveedorAutenticacion.emitirRefreshToken(usuario)).thenReturn("refresh_token_jwt");
        when(flotaRepository.buscarPorId(flotaId)).thenReturn(Optional.of(flota));

        // Act
        RespuestaToken respuesta = service.iniciarSesion("laura@tandinos.co", "Password123");

        // Assert
        assertNotNull(respuesta);
        assertEquals("access_token_jwt", respuesta.accessToken());
        assertEquals("refresh_token_jwt", respuesta.refreshToken());
        assertEquals("Bearer", respuesta.tipo());
        assertEquals(3600L, respuesta.expiracionSegundos());
        assertEquals(RolUsuario.DESPACHADOR, respuesta.rol());
        assertEquals("Laura Gómez", respuesta.nombre());
        assertEquals("Transportes Andinos", respuesta.nombreFlota());
        assertFalse(respuesta.debeCambiarPassword());

        verify(proveedorAutenticacion, times(1)).emitirToken(usuario);
        verify(proveedorAutenticacion, times(1)).emitirRefreshToken(usuario);
    }

    @Test
    void iniciarSesion_usuarioInexistente_lanzaBadCredentialsYSinEmitirTokens() {
        // Arrange
        when(usuarioRepository.buscarPorEmail("noexiste@tire.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BadCredentialsException.class,
                () -> service.iniciarSesion("noexiste@tire.com", "Password123"));
        verify(proveedorAutenticacion, never()).emitirToken(any());
        verify(proveedorAutenticacion, never()).emitirRefreshToken(any());
    }

    @Test
    void iniciarSesion_usuarioInactivo_lanzaBadCredentialsYSinEmitirTokens() {
        // Arrange
        Usuario usuario = new Usuario(
                usuarioId, "inactivo@tire.com", "hashed", "Inactivo",
                RolUsuario.CONDUCTOR, null, false, false
        );
        when(usuarioRepository.buscarPorEmail("inactivo@tire.com")).thenReturn(Optional.of(usuario));

        // Act & Assert
        assertThrows(BadCredentialsException.class,
                () -> service.iniciarSesion("inactivo@tire.com", "Password123"));
        verify(proveedorAutenticacion, never()).emitirToken(any());
    }

    @Test
    void iniciarSesion_passwordIncorrecta_lanzaBadCredentialsYSinEmitirTokens() {
        // Arrange
        Usuario usuario = new Usuario(
                usuarioId, "laura@tandinos.co", "hashed123", "Laura Gómez",
                RolUsuario.DESPACHADOR, null, true, false
        );
        when(usuarioRepository.buscarPorEmail("laura@tandinos.co")).thenReturn(Optional.of(usuario));
        when(proveedorAutenticacion.verificarContrasena("PasswordErronea", "hashed123")).thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class,
                () -> service.iniciarSesion("laura@tandinos.co", "PasswordErronea"));
        verify(proveedorAutenticacion, never()).emitirToken(any());
    }

    @Test
    void renovarSesion_refreshTokenValido_emiteNuevosTokens() {
        // Arrange
        Usuario usuario = new Usuario(
                usuarioId, "laura@tandinos.co", "hashed123", "Laura Gómez",
                RolUsuario.DESPACHADOR, null, true, false
        );
        when(proveedorAutenticacion.validarRefreshToken("valid_refresh_token")).thenReturn(usuarioId);
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(proveedorAutenticacion.emitirToken(usuario)).thenReturn("new_access_token");
        when(proveedorAutenticacion.emitirRefreshToken(usuario)).thenReturn("new_refresh_token");

        // Act
        RespuestaToken respuesta = service.renovarSesion("valid_refresh_token");

        // Assert
        assertNotNull(respuesta);
        assertEquals("new_access_token", respuesta.accessToken());
        assertEquals("new_refresh_token", respuesta.refreshToken());
        verify(proveedorAutenticacion, times(1)).emitirToken(usuario);
    }

    @Test
    void renovarSesion_refreshTokenInvalido_lanzaBadCredentials() {
        // Arrange
        when(proveedorAutenticacion.validarRefreshToken("bad_token"))
                .thenThrow(new RuntimeException("Token expirado"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> service.renovarSesion("bad_token"));
        verify(proveedorAutenticacion, never()).emitirToken(any());
    }

    @Test
    void cambiarContrasena_actualValidaYNuevaValida_actualizaPasswordYPersiste() {
        // Arrange
        Usuario usuario = new Usuario(
                usuarioId, "laura@tandinos.co", "oldHash", "Laura Gómez",
                RolUsuario.DESPACHADOR, null, true, true
        );
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(proveedorAutenticacion.verificarContrasena("PasswordActual1", "oldHash")).thenReturn(true);
        when(proveedorAutenticacion.hashearContrasena("NuevaPassword2")).thenReturn("newHash");

        // Act
        service.cambiarContrasena(usuarioId, "PasswordActual1", "NuevaPassword2");

        // Assert
        assertEquals("newHash", usuario.getPasswordHash());
        assertFalse(usuario.isDebeCambiarPassword());
        verify(usuarioRepository, times(1)).guardar(usuario);
    }

    @Test
    void cambiarContrasena_actualInvalida_lanzaBadCredentials() {
        // Arrange
        Usuario usuario = new Usuario(
                usuarioId, "laura@tandinos.co", "oldHash", "Laura Gómez",
                RolUsuario.DESPACHADOR, null, true, true
        );
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(proveedorAutenticacion.verificarContrasena("MalPassword", "oldHash")).thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class,
                () -> service.cambiarContrasena(usuarioId, "MalPassword", "NuevaPassword2"));
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void cambiarContrasena_nuevaPasswordInvalida_lanzaReglaNegocio() {
        // Arrange
        Usuario usuario = new Usuario(
                usuarioId, "laura@tandinos.co", "oldHash", "Laura Gómez",
                RolUsuario.DESPACHADOR, null, true, true
        );
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(proveedorAutenticacion.verificarContrasena("PasswordActual1", "oldHash")).thenReturn(true);

        // Menos de 8 caracteres
        assertThrows(ReglaNegocioException.class,
                () -> service.cambiarContrasena(usuarioId, "PasswordActual1", "Corta1"));

        // Igual a la actual
        assertThrows(ReglaNegocioException.class,
                () -> service.cambiarContrasena(usuarioId, "PasswordActual1", "PasswordActual1"));

        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void recuperacion_solicitarYConfirmar_actualizaPasswordExitosamente() {
        // Arrange
        Usuario usuario = new Usuario(
                usuarioId, "recuperar@tire.com", "oldHash", "Usuario Recuperar",
                RolUsuario.CONDUCTOR, null, true, false
        );
        when(usuarioRepository.buscarPorEmail("recuperar@tire.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(proveedorAutenticacion.hashearContrasena("NuevaClaveSegura123")).thenReturn("nuevaClaveHash");

        // Act 1: Solicitar recuperacion
        service.solicitarRecuperacion("recuperar@tire.com");

        // Extraer token generado en memoria
        try {
            java.lang.reflect.Field field = AutenticacionService.class.getDeclaredField("tokensRecuperacion");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, ?> tokens = (java.util.Map<String, ?>) field.get(service);
            assertFalse(tokens.isEmpty());
            String tokenGenerado = tokens.keySet().iterator().next();

            // Act 2: Confirmar recuperacion
            service.confirmarRecuperacion(tokenGenerado, "NuevaClaveSegura123");

            // Assert
            assertEquals("nuevaClaveHash", usuario.getPasswordHash());
            verify(usuarioRepository, times(1)).guardar(usuario);
            assertTrue(tokens.isEmpty(), "El token de un solo uso debe removerse tras su uso");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void confirmarRecuperacion_tokenInvalidoOInexistente_lanzaTokenRecuperacionInvalido() {
        // Act & Assert
        assertThrows(TokenRecuperacionInvalidoException.class,
                () -> service.confirmarRecuperacion("token_inventado", "NuevaClaveSegura123"));
        verify(usuarioRepository, never()).guardar(any());
    }
}
