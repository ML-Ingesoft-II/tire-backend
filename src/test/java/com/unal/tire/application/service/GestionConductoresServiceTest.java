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

import com.unal.tire.application.port.in.ComandoActualizarConductor;
import com.unal.tire.application.port.in.ComandoCrearConductor;
import com.unal.tire.application.port.in.GestionConductoresUseCase.ConductorVista;
import com.unal.tire.application.port.in.GestionConductoresUseCase.PaginaConductores;
import com.unal.tire.application.port.out.ConductorRepositoryPort;
import com.unal.tire.application.port.out.ProveedorAutenticacionPort;
import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.application.port.out.ViajeRepositoryPort;
import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.model.PerfilConductor;
import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.valueobject.RolUsuario;
import com.unal.tire.domain.valueobject.TipoDocumento;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GestionConductoresServiceTest {

    @Mock
    private ConductorRepositoryPort conductorRepository;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private ProveedorAutenticacionPort proveedorAutenticacion;

    @Mock
    private ViajeRepositoryPort viajeRepository;

    private GestionConductoresService service;

    private final UUID flotaId = UUID.randomUUID();
    private final UUID conductorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GestionConductoresService(
                conductorRepository,
                usuarioRepository,
                proveedorAutenticacion,
                viajeRepository
        );
    }

    @Test
    void crear_conductorValido_persisteUsuarioYPerfilYRetornaVista() {
        // Arrange
        ComandoCrearConductor comando = new ComandoCrearConductor(
                "Carlos",
                "Pérez Gómez",
                TipoDocumento.CC,
                "1020304050",
                "cperez@tandinos.co",
                "3001234567",
                "XYZ123",
                flotaId
        );

        when(conductorRepository.existePorDocumento(comando.numeroDocumento())).thenReturn(false);
        when(usuarioRepository.existePorEmail(comando.correo())).thenReturn(false);
        when(proveedorAutenticacion.hashearContrasena(anyString())).thenReturn("hashedPassword123");
        when(conductorRepository.guardar(any(PerfilConductor.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ConductorVista resultado = service.crear(comando);

        // Assert
        assertNotNull(resultado);
        assertEquals("Carlos Pérez Gómez", resultado.perfil().getNombreCompleto());
        assertEquals(comando.correo(), resultado.correo());
        assertFalse(resultado.tieneViajeEnCurso());

        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository, times(1)).guardar(usuarioCaptor.capture());
        Usuario usuarioGuardado = usuarioCaptor.getValue();
        assertEquals(comando.correo(), usuarioGuardado.getEmail());
        assertEquals("hashedPassword123", usuarioGuardado.getPasswordHash());
        assertEquals(RolUsuario.CONDUCTOR, usuarioGuardado.getRol());
        assertEquals(flotaId, usuarioGuardado.getFlotaId());
        assertTrue(usuarioGuardado.isDebeCambiarPassword());

        ArgumentCaptor<PerfilConductor> perfilCaptor = ArgumentCaptor.forClass(PerfilConductor.class);
        verify(conductorRepository, times(1)).guardar(perfilCaptor.capture());
        PerfilConductor perfilGuardado = perfilCaptor.getValue();
        assertEquals(usuarioGuardado.getId(), perfilGuardado.getUsuarioId());
        assertEquals("Carlos", perfilGuardado.getNombres());
        assertEquals("1020304050", perfilGuardado.getNumeroDocumento());
        assertEquals("XYZ123", perfilGuardado.getPlacaVehiculo());
        assertTrue(perfilGuardado.isActivo());
    }

    @Test
    void crear_documentoDuplicado_lanzaConflictoYSinGuardar() {
        // Arrange
        ComandoCrearConductor comando = new ComandoCrearConductor(
                "Carlos", "Pérez", TipoDocumento.CC, "1020304050",
                "cperez@tandinos.co", null, null, flotaId
        );
        when(conductorRepository.existePorDocumento(comando.numeroDocumento())).thenReturn(true);

        // Act & Assert
        ConflictoEstadoException ex = assertThrows(ConflictoEstadoException.class, () -> service.crear(comando));
        assertEquals("Ya existe un conductor con ese documento", ex.getMessage());

        verify(usuarioRepository, never()).guardar(any());
        verify(conductorRepository, never()).guardar(any());
    }

    @Test
    void crear_correoDuplicado_lanzaConflictoYSinGuardar() {
        // Arrange
        ComandoCrearConductor comando = new ComandoCrearConductor(
                "Carlos", "Pérez", TipoDocumento.CC, "1020304050",
                "cperez@tandinos.co", null, null, flotaId
        );
        when(conductorRepository.existePorDocumento(comando.numeroDocumento())).thenReturn(false);
        when(usuarioRepository.existePorEmail(comando.correo())).thenReturn(true);

        // Act & Assert
        ConflictoEstadoException ex = assertThrows(ConflictoEstadoException.class, () -> service.crear(comando));
        assertEquals("Ya existe un conductor con ese correo", ex.getMessage());

        verify(usuarioRepository, never()).guardar(any());
        verify(conductorRepository, never()).guardar(any());
    }

    @Test
    void obtenerPorId_conductorInexistente_lanzaRecursoNoEncontrado() {
        // Arrange
        when(conductorRepository.buscarPorId(conductorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(flotaId, conductorId));
    }

    @Test
    void obtenerPorId_conductorDeOtraFlota_lanzaRecursoNoEncontrado() {
        // Arrange
        UUID otraFlotaId = UUID.randomUUID();
        PerfilConductor perfilOtraFlota = new PerfilConductor(
                conductorId, "Juan", "López", TipoDocumento.CC, "98765432",
                "3120000000", "ABC111", otraFlotaId, true, LocalDateTime.now()
        );
        when(conductorRepository.buscarPorId(conductorId)).thenReturn(Optional.of(perfilOtraFlota));

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerPorId(flotaId, conductorId));
    }

    @Test
    void obtenerPorId_conductorValidoMismaFlota_retornaVista() {
        // Arrange
        PerfilConductor perfil = new PerfilConductor(
                conductorId, "Juan", "López", TipoDocumento.CC, "98765432",
                "3120000000", "ABC111", flotaId, true, LocalDateTime.now()
        );
        Usuario usuario = new Usuario(
                conductorId, "jlopez@tandinos.co", "hash", "Juan López",
                RolUsuario.CONDUCTOR, flotaId, true, false
        );

        when(conductorRepository.buscarPorId(conductorId)).thenReturn(Optional.of(perfil));
        when(usuarioRepository.buscarPorId(conductorId)).thenReturn(Optional.of(usuario));
        when(viajeRepository.existeViajeEnCursoParaConductor(conductorId)).thenReturn(true);

        // Act
        ConductorVista vista = service.obtenerPorId(flotaId, conductorId);

        // Assert
        assertNotNull(vista);
        assertEquals(conductorId, vista.perfil().getUsuarioId());
        assertEquals("Juan López", vista.perfil().getNombreCompleto());
        assertEquals("jlopez@tandinos.co", vista.correo());
        assertTrue(vista.tieneViajeEnCurso());
    }

    @Test
    void actualizar_datosValidos_persisteCambiosEnPerfilYUsuario() {
        // Arrange
        PerfilConductor perfilExistente = new PerfilConductor(
                conductorId, "Andrés", "Pineda", TipoDocumento.CC, "80412337",
                "3115550142", "TKL118", flotaId, true, LocalDateTime.now()
        );
        Usuario usuarioExistente = new Usuario(
                conductorId, "apineda@tandinos.co", "hash", "Andrés Pineda",
                RolUsuario.CONDUCTOR, flotaId, true, false
        );

        when(conductorRepository.buscarPorId(conductorId)).thenReturn(Optional.of(perfilExistente));
        when(conductorRepository.guardar(any(PerfilConductor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.buscarPorId(conductorId)).thenReturn(Optional.of(usuarioExistente));
        when(viajeRepository.existeViajeEnCursoParaConductor(conductorId)).thenReturn(false);

        ComandoActualizarConductor comando = new ComandoActualizarConductor(
                "Andrés Felipe",
                "Pineda Castro",
                TipoDocumento.CC,
                "80412337",
                "afpineda@tandinos.co",
                "3205550999",
                "NEW999",
                true
        );

        // Act
        ConductorVista resultado = service.actualizar(flotaId, conductorId, comando);

        // Assert
        assertNotNull(resultado);
        verify(conductorRepository, times(1)).guardar(perfilExistente);
        verify(usuarioRepository, times(1)).guardar(usuarioExistente);
        assertEquals("Andrés Felipe Pineda Castro", perfilExistente.getNombreCompleto());
        assertEquals("NEW999", perfilExistente.getPlacaVehiculo());
        assertEquals("afpineda@tandinos.co", usuarioExistente.getEmail());
    }

    @Test
    void actualizar_desactivarConViajeEnCurso_lanzaConflicto() {
        // Arrange
        PerfilConductor perfil = new PerfilConductor(
                conductorId, "Andrés", "Pineda", TipoDocumento.CC, "80412337",
                "3115550142", "TKL118", flotaId, true, LocalDateTime.now()
        );
        when(conductorRepository.buscarPorId(conductorId)).thenReturn(Optional.of(perfil));
        when(viajeRepository.existeViajeEnCursoParaConductor(conductorId)).thenReturn(true);

        ComandoActualizarConductor comando = new ComandoActualizarConductor(
                null, null, null, null, null, null, null, false
        );

        // Act & Assert
        ConflictoEstadoException ex = assertThrows(ConflictoEstadoException.class,
                () -> service.actualizar(flotaId, conductorId, comando));
        assertEquals("Este conductor tiene un viaje en curso", ex.getMessage());
        verify(conductorRepository, never()).guardar(any());
    }

    @Test
    void desactivar_conductorSinViaje_cambiaEstadoAInactivo() {
        // Arrange
        PerfilConductor perfil = new PerfilConductor(
                conductorId, "Andrés", "Pineda", TipoDocumento.CC, "80412337",
                "3115550142", "TKL118", flotaId, true, LocalDateTime.now()
        );
        Usuario usuario = new Usuario(
                conductorId, "apineda@tandinos.co", "hash", "Andrés Pineda",
                RolUsuario.CONDUCTOR, flotaId, true, false
        );

        when(conductorRepository.buscarPorId(conductorId)).thenReturn(Optional.of(perfil));
        when(viajeRepository.existeViajeEnCursoParaConductor(conductorId)).thenReturn(false);
        when(conductorRepository.guardar(any(PerfilConductor.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.buscarPorId(conductorId)).thenReturn(Optional.of(usuario));

        // Act
        ConductorVista resultado = service.desactivar(flotaId, conductorId);

        // Assert
        assertNotNull(resultado);
        assertFalse(perfil.isActivo());
        assertFalse(usuario.isActivo());
        verify(conductorRepository, times(1)).guardar(perfil);
        verify(usuarioRepository, times(1)).guardar(usuario);
    }

    @Test
    void desactivar_conductorConViajeActivo_lanzaConflictoYSinGuardar() {
        // Arrange
        PerfilConductor perfil = new PerfilConductor(
                conductorId, "Andrés", "Pineda", TipoDocumento.CC, "80412337",
                "3115550142", "TKL118", flotaId, true, LocalDateTime.now()
        );
        when(conductorRepository.buscarPorId(conductorId)).thenReturn(Optional.of(perfil));
        when(viajeRepository.existeViajeEnCursoParaConductor(conductorId)).thenReturn(true);

        // Act & Assert
        ConflictoEstadoException ex = assertThrows(ConflictoEstadoException.class,
                () -> service.desactivar(flotaId, conductorId));
        assertEquals("Este conductor tiene un viaje en curso", ex.getMessage());
        verify(conductorRepository, never()).guardar(any());
    }

    @Test
    void listar_conFiltrosYPaginacion_retornaPaginaFiltrada() {
        // Arrange
        PerfilConductor c1 = new PerfilConductor(
                UUID.randomUUID(), "Carlos", "Rodríguez", TipoDocumento.CC, "1019874552",
                "3005550119", "TSK482", flotaId, true, LocalDateTime.now()
        );
        PerfilConductor c2 = new PerfilConductor(
                UUID.randomUUID(), "Diana", "Morales", TipoDocumento.CC, "52903118",
                "3155550176", "SPQ903", flotaId, false, LocalDateTime.now()
        );

        when(conductorRepository.listarPorFlota(flotaId)).thenReturn(List.of(c1, c2));
        when(usuarioRepository.buscarPorId(c1.getUsuarioId())).thenReturn(Optional.of(
                new Usuario(c1.getUsuarioId(), "crodriguez@tandinos.co", "h", "Carlos Rodríguez", RolUsuario.CONDUCTOR, flotaId, true, false)
        ));
        when(viajeRepository.existeViajeEnCursoParaConductor(c1.getUsuarioId())).thenReturn(false);

        // Act - Filtrar solo activos y buscar 'carlos'
        PaginaConductores pagina = service.listar(flotaId, "carlos", true, 0, 10);

        // Assert
        assertEquals(1, pagina.totalElementos());
        assertEquals(1, pagina.contenido().size());
        assertEquals("Carlos Rodríguez", pagina.contenido().get(0).perfil().getNombreCompleto());
    }
}
