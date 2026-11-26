package com.unal.tire.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unal.tire.application.port.in.GestionAlertasUseCase.AlertaDetalleVista;
import com.unal.tire.application.port.in.GestionAlertasUseCase.AlertaVista;
import com.unal.tire.application.port.in.GestionAlertasUseCase.EstadoFlota;
import com.unal.tire.application.port.in.GestionAlertasUseCase.PaginaEventos;
import com.unal.tire.application.port.out.AlertaCriticaRepositoryPort;
import com.unal.tire.application.port.out.ConductorRepositoryPort;
import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.application.port.out.ViajeRepositoryPort;
import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.exception.ReglaNegocioException;
import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.model.PerfilConductor;
import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.CondicionClimatica;
import com.unal.tire.domain.valueobject.EstadoMonitoreo;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import com.unal.tire.domain.valueobject.RolUsuario;
import com.unal.tire.domain.valueobject.TipoDocumento;
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
class GestionAlertasServiceTest {

    @Mock
    private ViajeRepositoryPort viajeRepository;

    @Mock
    private AlertaCriticaRepositoryPort alertaRepository;

    @Mock
    private ConductorRepositoryPort conductorRepository;

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private GestionAlertasService service;

    private final UUID flotaId = UUID.randomUUID();
    private final UUID conductor1Id = UUID.randomUUID();
    private final UUID conductor2Id = UUID.randomUUID();
    private final UUID alertaId = UUID.randomUUID();
    private final UUID viajeId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new GestionAlertasService(
                viajeRepository,
                alertaRepository,
                conductorRepository,
                usuarioRepository
        );
    }

    private AlertaCritica crearAlerta(boolean atendida) {
        return new AlertaCritica(
                alertaId,
                viajeId,
                conductor1Id,
                flotaId,
                UUID.randomUUID(),
                TipoEvento.MICROSUENO,
                2.5,
                LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().minusMinutes(4),
                atendida ? LocalDateTime.now().minusMinutes(1) : null,
                new Ubicacion(5.1462, -73.6851),
                atendida,
                atendida ? UUID.randomUUID() : null,
                atendida ? "Despachador Uno" : null,
                atendida ? "Conductor atendido y seguro" : null
        );
    }

    @Test
    void obtenerEstadoFlota_conductoresEnViajeYSinViaje_calculaConteosYOrdenaPorGravedad() {
        // Arrange
        PerfilConductor p1 = new PerfilConductor(
                conductor1Id, "Andrés", "Pineda", TipoDocumento.CC, "80412337",
                "3115550142", "TKL118", flotaId, true, LocalDateTime.now()
        );
        PerfilConductor p2 = new PerfilConductor(
                conductor2Id, "Carlos", "Rodríguez", TipoDocumento.CC, "1019874552",
                "3005550119", "TSK482", flotaId, true, LocalDateTime.now()
        );

        when(conductorRepository.listarActivosPorFlota(flotaId)).thenReturn(List.of(p1, p2));

        Viaje v1 = new Viaje(
                viajeId, conductor1Id, "Bogotá", "Tunja", EstadoViaje.EN_CURSO,
                NivelAlerta.CRITICO, LocalDateTime.now().minusHours(2), null,
                new Ubicacion(5.1462, -73.6851), new CondicionClimatica(18.0, 10.0, "DESPEJADO"),
                LocalDateTime.now().minusSeconds(10), 75.0, new ArrayList<>()
        );

        when(viajeRepository.listarPorConductores(any())).thenReturn(List.of(v1));

        AlertaCritica alerta = crearAlerta(false);
        when(alertaRepository.listarPorFlotaYAtendida(flotaId, false)).thenReturn(List.of(alerta));

        // Act
        EstadoFlota estado = service.obtenerEstadoFlota(flotaId);

        // Assert
        assertNotNull(estado);
        assertEquals(1, estado.conductoresEnViaje().size());
        assertEquals(1, estado.conductoresSinViaje().size());
        assertEquals(1, estado.conteoPorEstado().get(EstadoMonitoreo.CRITICO));
        assertEquals(1, estado.conteoPorEstado().get(EstadoMonitoreo.SIN_VIAJE));

        var conductorEnViaje = estado.conductoresEnViaje().get(0);
        assertEquals(conductor1Id, conductorEnViaje.conductor().id());
        assertEquals(EstadoMonitoreo.CRITICO, conductorEnViaje.estado());
        assertEquals(alertaId, conductorEnViaje.alertaAbiertaId());
    }

    @Test
    void listarAlertasAbiertas_flotaConAlertas_retornaListaOrdenadaConResumen() {
        // Arrange
        AlertaCritica alerta = crearAlerta(false);
        when(alertaRepository.listarPorFlotaYAtendida(flotaId, false)).thenReturn(List.of(alerta));

        PerfilConductor p1 = new PerfilConductor(
                conductor1Id, "Andrés", "Pineda", TipoDocumento.CC, "80412337",
                "3115550142", "TKL118", flotaId, true, LocalDateTime.now()
        );
        when(conductorRepository.buscarPorId(conductor1Id)).thenReturn(Optional.of(p1));

        // Act
        List<AlertaVista> alertas = service.listarAlertasAbiertas(flotaId);

        // Assert
        assertNotNull(alertas);
        assertEquals(1, alertas.size());
        assertEquals(alertaId, alertas.get(0).alerta().getId());
        assertEquals("Andrés Pineda", alertas.get(0).conductor().nombreCompleto());
    }

    @Test
    void obtenerAlerta_alertaExistenteMismaFlota_retornaDetalleConLineaTiempo() {
        // Arrange
        AlertaCritica alerta = crearAlerta(true);
        when(alertaRepository.buscarPorId(alertaId)).thenReturn(Optional.of(alerta));

        Viaje viaje = new Viaje(
                viajeId, conductor1Id, "Bogotá", "Tunja", EstadoViaje.EN_CURSO,
                NivelAlerta.CRITICO, LocalDateTime.now().minusHours(2), null,
                new Ubicacion(5.1462, -73.6851), null, LocalDateTime.now().minusSeconds(10),
                70.0, new ArrayList<>()
        );
        when(viajeRepository.buscarPorId(viajeId)).thenReturn(Optional.of(viaje));

        PerfilConductor p1 = new PerfilConductor(
                conductor1Id, "Andrés", "Pineda", TipoDocumento.CC, "80412337",
                "3115550142", "TKL118", flotaId, true, LocalDateTime.now()
        );
        when(conductorRepository.buscarPorId(conductor1Id)).thenReturn(Optional.of(p1));

        // Act
        AlertaDetalleVista detalle = service.obtenerAlerta(flotaId, alertaId);

        // Assert
        assertNotNull(detalle);
        assertEquals(alertaId, detalle.alerta().getId());
        assertEquals("Andrés Pineda", detalle.conductor().nombreCompleto());
        assertNotNull(detalle.fechaInicioViaje());
        assertFalse(detalle.lineaTiempo().isEmpty());
        assertTrue(detalle.lineaTiempo().stream().anyMatch(e -> "DETECTADO".equals(e.tipo())));
        assertTrue(detalle.lineaTiempo().stream().anyMatch(e -> "ATENDIDA".equals(e.tipo())));
    }

    @Test
    void obtenerAlerta_alertaDeOtraFlotaOInexistente_lanzaRecursoNoEncontrado() {
        // Inexistente
        when(alertaRepository.buscarPorId(alertaId)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerAlerta(flotaId, alertaId));

        // De otra flota
        UUID otraFlotaId = UUID.randomUUID();
        AlertaCritica alertaOtraFlota = new AlertaCritica(
                alertaId, viajeId, conductor1Id, otraFlotaId, UUID.randomUUID(),
                TipoEvento.MICROSUENO, 2.0, LocalDateTime.now(), LocalDateTime.now(),
                null, null, false, null, null, null
        );
        when(alertaRepository.buscarPorId(alertaId)).thenReturn(Optional.of(alertaOtraFlota));
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtenerAlerta(flotaId, alertaId));
    }

    @Test
    void atenderAlerta_alertaAbierta_marcaAtendidaConDespachadorYPersiste() {
        // Arrange
        AlertaCritica alerta = crearAlerta(false);
        when(alertaRepository.buscarPorId(alertaId)).thenReturn(Optional.of(alerta));
        when(alertaRepository.guardar(any(AlertaCritica.class))).thenAnswer(inv -> inv.getArgument(0));

        UUID despachadorId = UUID.randomUUID();
        Usuario despachador = new Usuario(
                despachadorId, "laura@tandinos.co", "h", "Laura Gómez",
                RolUsuario.DESPACHADOR, flotaId, true, false
        );
        when(usuarioRepository.buscarPorId(despachadorId)).thenReturn(Optional.of(despachador));

        PerfilConductor p1 = new PerfilConductor(
                conductor1Id, "Andrés", "Pineda", TipoDocumento.CC, "80412337",
                "3115550142", "TKL118", flotaId, true, LocalDateTime.now()
        );
        when(conductorRepository.buscarPorId(conductor1Id)).thenReturn(Optional.of(p1));

        // Act
        AlertaVista resultado = service.atenderAlerta(
                flotaId, despachadorId, "laura@tandinos.co", alertaId, "Se contactó al conductor por radio."
        );

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.alerta().isAtendida());
        assertEquals("Laura Gómez", resultado.alerta().getAtendidaPorNombre());
        assertEquals("Se contactó al conductor por radio.", resultado.alerta().getNotasDespachador());
    }

    @Test
    void atenderAlerta_alertaYaAtendida_lanzaConflictoYSinSobrescribir() {
        // Arrange
        AlertaCritica alerta = crearAlerta(true); // Ya atendida
        when(alertaRepository.buscarPorId(alertaId)).thenReturn(Optional.of(alerta));
        when(usuarioRepository.buscarPorId(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ConflictoEstadoException.class, () -> service.atenderAlerta(
                flotaId, UUID.randomUUID(), "otro@tandinos.co", alertaId, "Segunda atencion"
        ));
        verify(alertaRepository, never()).guardar(any());
    }

    @Test
    void historialEventos_fechaFinAnteriorAInicio_lanzaReglaNegocio() {
        // Arrange
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fin = inicio.minusHours(1);

        // Act & Assert
        assertThrows(ReglaNegocioException.class, () -> service.historialEventos(
                flotaId, inicio, fin, null, null, 0, 10
        ));
    }

    @Test
    void historialEventos_filtrosValidos_retornaPaginaEventosOrdenada() {
        // Arrange
        PerfilConductor p1 = new PerfilConductor(
                conductor1Id, "Andrés", "Pineda", TipoDocumento.CC, "80412337",
                "3115550142", "TKL118", flotaId, true, LocalDateTime.now()
        );
        when(conductorRepository.listarPorFlota(flotaId)).thenReturn(List.of(p1));

        AlertaCritica alerta = crearAlerta(true);
        when(alertaRepository.listarPorFlota(flotaId)).thenReturn(List.of(alerta));

        EventoFatiga evento1 = new EventoFatiga(
                alerta.getEventoId(), viajeId, TipoEvento.MICROSUENO, LocalDateTime.now().minusHours(1),
                new Ubicacion(5.1, -73.6), 2.2, true, 3.0, null
        );
        Viaje v1 = new Viaje(
                viajeId, conductor1Id, "Bogotá", "Tunja", EstadoViaje.EN_CURSO,
                NivelAlerta.CRITICO, LocalDateTime.now().minusHours(2), null,
                new Ubicacion(5.1, -73.6), null, LocalDateTime.now(), 70.0, List.of(evento1)
        );

        when(viajeRepository.listarPorConductores(List.of(conductor1Id))).thenReturn(List.of(v1));

        // Act
        PaginaEventos pagina = service.historialEventos(flotaId, null, null, TipoEvento.MICROSUENO, conductor1Id, 0, 10);

        // Assert
        assertNotNull(pagina);
        assertEquals(1, pagina.totalElementos());
        assertEquals(1, pagina.contenido().size());
        assertEquals("ATENDIDA", pagina.contenido().get(0).atencion());
        assertEquals("Andrés Pineda", pagina.contenido().get(0).conductor().nombreCompleto());
    }
}
