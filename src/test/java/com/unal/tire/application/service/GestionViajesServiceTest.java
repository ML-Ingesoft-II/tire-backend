package com.unal.tire.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unal.tire.application.port.in.ComandoConfirmarAlerta;
import com.unal.tire.application.port.in.ComandoEventoFatiga;
import com.unal.tire.application.port.in.ComandoIniciarViaje;
import com.unal.tire.application.port.in.ComandoTelemetria;
import com.unal.tire.application.port.in.GestionViajesUseCase.PaginaViajes;
import com.unal.tire.application.port.out.AlertaCriticaRepositoryPort;
import com.unal.tire.application.port.out.ConsultorClimaPort;
import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.application.port.out.ViajeRepositoryPort;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import com.unal.tire.domain.valueobject.RolUsuario;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
class GestionViajesServiceTest {

    @Mock
    private ViajeRepositoryPort viajeRepository;

    @Mock
    private AlertaCriticaRepositoryPort alertaRepository;

    @Mock
    private ConsultorClimaPort consultorClima;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private GestionViajesService service;

    private final UUID conductorId = UUID.randomUUID();
    private final UUID viajeId = UUID.randomUUID();
    private final UUID flotaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GestionViajesService(
                viajeRepository,
                alertaRepository,
                consultorClima,
                usuarioRepository
        );
    }

    private Viaje crearViajeEnCurso(NivelAlerta nivelAlerta) {
        return new Viaje(
                viajeId,
                conductorId,
                "Bogotá",
                "Tunja",
                EstadoViaje.EN_CURSO,
                nivelAlerta,
                LocalDateTime.now().minusHours(1),
                null,
                new Ubicacion(4.711, -74.072),
                new CondicionClimatica(18.0, 10.0, "DESPEJADO"),
                LocalDateTime.now().minusSeconds(10),
                60.0,
                new ArrayList<>()
        );
    }

    @Test
    void iniciarViaje_datosValidosConClima_persisteViajeYRetornaAgregado() {
        // Arrange
        ComandoIniciarViaje comando = new ComandoIniciarViaje(
                conductorId, "Bogotá", "Medellín", 4.711, -74.072
        );
        when(viajeRepository.buscarEnCursoDeConductor(conductorId)).thenReturn(Optional.empty());
        when(consultorClima.consultarClimaActual(4.711, -74.072))
                .thenReturn(new CondicionClimatica(19.5, 12.0, "PARCIALMENTE_NUBLADO"));
        when(viajeRepository.guardar(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Viaje viaje = service.iniciarViaje(comando);

        // Assert
        assertNotNull(viaje);
        assertEquals(conductorId, viaje.getConductorId());
        assertEquals("Bogotá", viaje.getOrigen());
        assertEquals("Medellín", viaje.getDestino());
        assertEquals(EstadoViaje.EN_CURSO, viaje.getEstado());
        assertEquals(NivelAlerta.NORMAL, viaje.getNivelAlerta());
        assertEquals(4.711, viaje.getUltimaUbicacion().latitud());
        assertEquals("PARCIALMENTE_NUBLADO", viaje.getUltimoClima().descripcion());

        verify(viajeRepository, times(1)).guardar(any(Viaje.class));
    }

    @Test
    void iniciarViaje_conductorYaTieneViajeActivo_lanzaViajeActivoExceptionYSinGuardar() {
        // Arrange
        ComandoIniciarViaje comando = new ComandoIniciarViaje(
                conductorId, "Bogotá", "Cali", null, null
        );
        Viaje viajeExistente = crearViajeEnCurso(NivelAlerta.NORMAL);
        when(viajeRepository.buscarEnCursoDeConductor(conductorId)).thenReturn(Optional.of(viajeExistente));

        // Act & Assert
        GestionViajesService.ViajeActivoException ex = assertThrows(
                GestionViajesService.ViajeActivoException.class,
                () -> service.iniciarViaje(comando)
        );
        assertEquals(viajeId, ex.getViajeId());
        assertEquals("El conductor ya tiene un viaje activo en curso", ex.getMessage());

        verify(viajeRepository, never()).guardar(any());
    }

    @Test
    void registrarTelemetria_viajeExistente_actualizaUbicacionVelocidadClimaYPersiste() {
        // Arrange
        Viaje viaje = crearViajeEnCurso(NivelAlerta.NORMAL);
        when(viajeRepository.buscarPorId(viajeId)).thenReturn(Optional.of(viaje));
        when(consultorClima.consultarClimaActual(anyDouble(), anyDouble()))
                .thenReturn(new CondicionClimatica(22.0, 5.0, "DESPEJADO"));
        when(viajeRepository.guardar(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));

        ComandoTelemetria comando = new ComandoTelemetria(4.900, -74.200, 75.5);

        // Act
        Viaje resultado = service.registrarTelemetria(conductorId, viajeId, comando);

        // Assert
        assertNotNull(resultado);
        assertEquals(4.900, resultado.getUltimaUbicacion().latitud());
        assertEquals(-74.200, resultado.getUltimaUbicacion().longitud());
        assertEquals(75.5, resultado.getVelocidadKmh());
        verify(viajeRepository, times(1)).guardar(viaje);
    }

    @Test
    void registrarEventoFatiga_microsuenoCriticoConFlota_escalaACriticoYAbreAlertaCritica() {
        // Arrange
        Viaje viaje = crearViajeEnCurso(NivelAlerta.NORMAL);
        Usuario usuarioConductor = new Usuario(
                conductorId, "cond@tandinos.co", "h", "Conductor Flota",
                RolUsuario.CONDUCTOR, flotaId, true, false
        );

        when(viajeRepository.buscarPorId(viajeId)).thenReturn(Optional.of(viaje));
        when(viajeRepository.guardar(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));
        when(consultorClima.consultarClimaActual(anyDouble(), anyDouble()))
                .thenReturn(new CondicionClimatica(17.0, 8.0, "LLUVIA"));
        when(usuarioRepository.buscarPorId(conductorId)).thenReturn(Optional.of(usuarioConductor));

        ComandoEventoFatiga comando = new ComandoEventoFatiga(
                TipoEvento.MICROSUENO,
                LocalDateTime.now(),
                2.4,
                false,
                null,
                5.1462,
                -73.6851
        );

        // Act
        Viaje resultado = service.registrarEventoFatiga(conductorId, viajeId, comando);

        // Assert
        assertNotNull(resultado);
        assertEquals(NivelAlerta.CRITICO, resultado.getNivelAlerta());
        assertEquals(1, resultado.getEventosFatiga().size());

        ArgumentCaptor<AlertaCritica> alertaCaptor = ArgumentCaptor.forClass(AlertaCritica.class);
        verify(alertaRepository, times(1)).guardar(alertaCaptor.capture());
        AlertaCritica alertaGuardada = alertaCaptor.getValue();
        assertEquals(viajeId, alertaGuardada.getViajeId());
        assertEquals(conductorId, alertaGuardada.getConductorId());
        assertEquals(flotaId, alertaGuardada.getFlotaId());
        assertEquals(TipoEvento.MICROSUENO, alertaGuardada.getTipoEvento());
        assertEquals(2.4, alertaGuardada.getDuracionSegundos());
    }

    @Test
    void registrarEventoFatiga_conductorParticularSinFlota_escalaACriticoSinAbrirAlerta() {
        // Arrange
        Viaje viaje = crearViajeEnCurso(NivelAlerta.NORMAL);
        Usuario usuarioParticular = new Usuario(
                conductorId, "particular@gmail.com", "h", "Conductor Particular",
                RolUsuario.CONDUCTOR, null, true, false
        );

        when(viajeRepository.buscarPorId(viajeId)).thenReturn(Optional.of(viaje));
        when(viajeRepository.guardar(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.buscarPorId(conductorId)).thenReturn(Optional.of(usuarioParticular));

        ComandoEventoFatiga comando = new ComandoEventoFatiga(
                TipoEvento.MICROSUENO, LocalDateTime.now(), 2.0, false, null, null, null
        );

        // Act
        Viaje resultado = service.registrarEventoFatiga(conductorId, viajeId, comando);

        // Assert
        assertEquals(NivelAlerta.CRITICO, resultado.getNivelAlerta());
        verify(alertaRepository, never()).guardar(any());
    }

    @Test
    void finalizarViaje_viajeEnCurso_pasaAFinalizadoYPersiste() {
        // Arrange
        Viaje viaje = crearViajeEnCurso(NivelAlerta.NORMAL);
        when(viajeRepository.buscarPorId(viajeId)).thenReturn(Optional.of(viaje));
        when(viajeRepository.guardar(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Viaje resultado = service.finalizarViaje(conductorId, viajeId);

        // Assert
        assertEquals(EstadoViaje.FINALIZADO, resultado.getEstado());
        assertNotNull(resultado.getFechaFin());
        verify(viajeRepository, times(1)).guardar(viaje);
    }

    @Test
    void confirmarAlerta_viajeExistente_bajaNivelAlertaYPersiste() {
        // Arrange
        Viaje viaje = crearViajeEnCurso(NivelAlerta.CRITICO);
        when(viajeRepository.buscarPorId(viajeId)).thenReturn(Optional.of(viaje));
        when(viajeRepository.guardar(any(Viaje.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Viaje resultado = service.confirmarAlerta(conductorId, viajeId, new ComandoConfirmarAlerta(3));

        // Assert
        assertEquals(NivelAlerta.PRECAUCION, resultado.getNivelAlerta());
        verify(viajeRepository, times(1)).guardar(viaje);
    }

    @Test
    void obtenerViajePorId_despachadorDeMismaFlota_retornaViaje() {
        // Arrange
        Viaje viaje = crearViajeEnCurso(NivelAlerta.NORMAL);
        when(viajeRepository.buscarPorId(viajeId)).thenReturn(Optional.of(viaje));
        when(usuarioRepository.buscarPorId(conductorId)).thenReturn(Optional.of(
                new Usuario(conductorId, "c@tandinos.co", "h", "C", RolUsuario.CONDUCTOR, flotaId, true, false)
        ));

        // Act
        Viaje resultado = service.obtenerViajePorId(UUID.randomUUID(), RolUsuario.DESPACHADOR, flotaId, viajeId);

        // Assert
        assertNotNull(resultado);
        assertEquals(viajeId, resultado.getId());
    }

    @Test
    void obtenerViajePorId_despachadorDeOtraFlota_lanzaRecursoNoEncontrado() {
        // Arrange
        Viaje viaje = crearViajeEnCurso(NivelAlerta.NORMAL);
        when(viajeRepository.buscarPorId(viajeId)).thenReturn(Optional.of(viaje));
        when(usuarioRepository.buscarPorId(conductorId)).thenReturn(Optional.of(
                new Usuario(conductorId, "c@tandinos.co", "h", "C", RolUsuario.CONDUCTOR, flotaId, true, false)
        ));

        // Act & Assert
        UUID otraFlotaId = UUID.randomUUID();
        assertThrows(RecursoNoEncontradoException.class,
                () -> service.obtenerViajePorId(UUID.randomUUID(), RolUsuario.DESPACHADOR, otraFlotaId, viajeId));
    }

    @Test
    void obtenerViajeActivo_sinViajeActivo_lanzaRecursoNoEncontrado() {
        // Arrange
        when(viajeRepository.buscarEnCursoDeConductor(conductorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerViajeActivo(conductorId));
    }

    @Test
    void listarViajes_despachador_retornaPaginaViajes() {
        // Arrange
        Viaje v1 = crearViajeEnCurso(NivelAlerta.NORMAL);
        when(usuarioRepository.listarPorFlotaYRol(flotaId, RolUsuario.CONDUCTOR)).thenReturn(List.of(
                new Usuario(conductorId, "c@tandinos.co", "h", "C", RolUsuario.CONDUCTOR, flotaId, true, false)
        ));
        when(viajeRepository.listarPorConductores(List.of(conductorId))).thenReturn(List.of(v1));

        // Act
        PaginaViajes pagina = service.listarViajes(UUID.randomUUID(), RolUsuario.DESPACHADOR, flotaId, null, 0, 10);

        // Assert
        assertEquals(1, pagina.totalElementos());
        assertEquals(1, pagina.contenido().size());
        assertEquals(viajeId, pagina.contenido().get(0).getId());
    }
}
