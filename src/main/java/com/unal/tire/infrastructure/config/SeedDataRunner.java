package com.unal.tire.infrastructure.config;

import com.unal.tire.application.port.out.AlertaCriticaRepositoryPort;
import com.unal.tire.application.port.out.ConductorRepositoryPort;
import com.unal.tire.application.port.out.FlotaRepositoryPort;
import com.unal.tire.application.port.out.ProveedorAutenticacionPort;
import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.application.port.out.ViajeRepositoryPort;
import com.unal.tire.domain.model.AlertaCritica;
import com.unal.tire.domain.model.EventoFatiga;
import com.unal.tire.domain.model.Flota;
import com.unal.tire.domain.model.PerfilConductor;
import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.model.Viaje;
import com.unal.tire.domain.valueobject.EstadoViaje;
import com.unal.tire.domain.valueobject.NivelAlerta;
import com.unal.tire.domain.valueobject.RolUsuario;
import com.unal.tire.domain.valueobject.TipoDocumento;
import com.unal.tire.domain.valueobject.TipoEvento;
import com.unal.tire.domain.valueobject.Ubicacion;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Siembra datos de ejemplo que reproducen el panel del despachador del mockup
 * (flota "Transportes Andinos", despachadora Laura Gomez y 24 conductores con
 * sus viajes y eventos).
 *
 * <p>Activo solo con el perfil {@code seed}. Idempotente: si ya existe la flota
 * no vuelve a sembrar. El perfil {@code seed} recrea el esquema desde cero
 * ({@code ddl-auto: create-drop}).</p>
 */
@Component
@Profile("seed")
public class SeedDataRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);

    private static final String NOMBRE_FLOTA = "Transportes Andinos";
    private static final String PASSWORD_DESPACHADOR = "Password123";

    private final FlotaRepositoryPort flotaRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ConductorRepositoryPort conductorRepository;
    private final ViajeRepositoryPort viajeRepository;
    private final AlertaCriticaRepositoryPort alertaRepository;
    private final ProveedorAutenticacionPort auth;

    public SeedDataRunner(FlotaRepositoryPort flotaRepository,
                          UsuarioRepositoryPort usuarioRepository,
                          ConductorRepositoryPort conductorRepository,
                          ViajeRepositoryPort viajeRepository,
                          AlertaCriticaRepositoryPort alertaRepository,
                          ProveedorAutenticacionPort auth) {
        this.flotaRepository = flotaRepository;
        this.usuarioRepository = usuarioRepository;
        this.conductorRepository = conductorRepository;
        this.viajeRepository = viajeRepository;
        this.alertaRepository = alertaRepository;
        this.auth = auth;
    }

    /** Datos de alta de un conductor del mockup. */
    private record DatosConductor(String nombres, String apellidos, String documento,
                                  String correo, String telefono, String placa) {
    }

    @Override
    public void run(String... args) {
        if (flotaRepository.buscarPorNombre(NOMBRE_FLOTA).isPresent()) {
            log.info("Seed: la flota '{}' ya existe; no se vuelve a sembrar.", NOMBRE_FLOTA);
            return;
        }
        log.info("Seed: sembrando datos de ejemplo (flota Transportes Andinos)...");

        Flota flota = flotaRepository.guardar(new Flota(UUID.randomUUID(), NOMBRE_FLOTA));
        UUID flotaId = flota.getId();

        // Despachadora (puede iniciar sesion en el panel).
        usuarioRepository.guardar(new Usuario(
                UUID.randomUUID(),
                "laura.gomez@tandinos.co",
                auth.hashearContrasena(PASSWORD_DESPACHADOR),
                "Laura Gómez",
                RolUsuario.DESPACHADOR,
                flotaId,
                true,
                false));

        List<DatosConductor> datos = List.of(
                new DatosConductor("Andrés", "Pineda", "80412337", "apineda@tandinos.co", "3115550142", "TKL-118"),
                new DatosConductor("Carlos", "Rodríguez", "1019874552", "crodriguez@tandinos.co", "3005550119", "TSK-482"),
                new DatosConductor("Diana", "Morales", "52903118", "dmorales@tandinos.co", "3155550176", "SPQ-903"),
                new DatosConductor("Jorge", "Ramírez", "79655201", "jramirez@tandinos.co", "3105550187", "WMR-215"),
                new DatosConductor("Luisa Fernanda", "Rojas", "1032448906", "lrojas@tandinos.co", "3205550133", "GHN-561"),
                new DatosConductor("Miguel Ángel", "Torres", "1110523874", "mtorres@tandinos.co", "3135550158", "BRT-334"),
                new DatosConductor("Óscar", "Beltrán", "74389660", "obeltran@tandinos.co", "3125550104", "XVF-772"),
                new DatosConductor("Paola", "Castro", "1022367415", "pcastro@tandinos.co", "3185550161", "JKM-209"),
                new DatosConductor("Natalia", "Suárez", "52188204", "nsuarez@tandinos.co", "3115550122", "FPL-412"),
                new DatosConductor("Fabián", "Ortiz", "80551903", "fortiz@tandinos.co", "3005550188", "HDN-728"),
                new DatosConductor("Sandra", "Patiño", "52771340", "spatino@tandinos.co", "3155550147", "MKR-156"),
                new DatosConductor("Hernán", "Quintero", "79442015", "hquintero@tandinos.co", "3125550129", "KTM-640"),
                new DatosConductor("Ricardo", "Gómez", "80233491", "rgomez@tandinos.co", "3105550111", "LQP-507"),
                new DatosConductor("Camila", "Vargas", "1019556201", "cvargas@tandinos.co", "3205550199", "RTB-882"),
                new DatosConductor("Julián", "Mejía", "79880345", "jmejia@tandinos.co", "3135550170", "NPC-314"),
                new DatosConductor("Daniela", "Rincón", "52990117", "drincon@tandinos.co", "3185550155", "VHG-629"),
                new DatosConductor("Esteban", "Cárdenas", "80112784", "ecardenas@tandinos.co", "3005550102", "ZWF-451"),
                new DatosConductor("Valentina", "Lozano", "1022998310", "vlozano@tandinos.co", "3115550133", "BKT-770"),
                new DatosConductor("Felipe", "Navarro", "79334620", "fnavarro@tandinos.co", "3125550166", "MSL-208"),
                new DatosConductor("Carolina", "Acosta", "52445109", "cacosta@tandinos.co", "3205550144", "TYU-915"),
                new DatosConductor("Andrés Felipe", "Duarte", "80667201", "aduarte@tandinos.co", "3135550121", "QER-603"),
                new DatosConductor("Manuela", "Ríos", "1019334870", "mrios@tandinos.co", "3185550177", "PLK-342"),
                new DatosConductor("Santiago", "Peña", "79556012", "speña@tandinos.co", "3105550109", "DFG-528"),
                new DatosConductor("Laura", "Jiménez", "52118903", "ljimenez@tandinos.co", "3005550155", "WSX-114"));

        List<PerfilConductor> conductores = new ArrayList<>();
        for (DatosConductor d : datos) {
            conductores.add(crearConductor(d, flotaId));
        }

        sembrarViajesYEventos(flotaId, conductores);

        log.info("Seed: completado. Flota={}, conductores={}.", flotaId, conductores.size());
    }

    private PerfilConductor crearConductor(DatosConductor d, UUID flotaId) {
        UUID id = UUID.randomUUID();
        String passwordInicial = capitalizar(d.apellidos().split(" ")[0])
                + ultimos4(d.documento());
        usuarioRepository.guardar(new Usuario(
                id,
                d.correo(),
                auth.hashearContrasena(passwordInicial),
                (d.nombres() + " " + d.apellidos()).trim(),
                RolUsuario.CONDUCTOR,
                flotaId,
                true,
                true));
        return conductorRepository.guardar(new PerfilConductor(
                id,
                d.nombres(),
                d.apellidos(),
                TipoDocumento.CC,
                d.documento(),
                d.telefono(),
                d.placa(),
                flotaId,
                true,
                LocalDateTime.now().minusMonths(3)));
    }

    private void sembrarViajesYEventos(UUID flotaId, List<PerfilConductor> c) {
        LocalDateTime ahora = LocalDateTime.now();

        // --- 13 conductores EN VIAJE (indices 0..12 de la lista) ---
        // Jorge Ramirez (idx 3): CRITICO, con alerta abierta.
        PerfilConductor jorge = c.get(3);
        Viaje vJorge = viajeEnCurso(jorge.getUsuarioId(), ahora.minusHours(3).minusMinutes(12),
                ahora.minusSeconds(4), 5.1462, -73.6851, NivelAlerta.CRITICO, 78.0);
        EventoFatiga evtJorge = new EventoFatiga(UUID.randomUUID(), vJorge.getId(),
                TipoEvento.MICROSUENO, ahora.minusSeconds(35), new Ubicacion(5.1462, -73.6851),
                2.3, false, null, null);
        vJorge.registrarEventoFatiga(evtJorge);
        vJorge = viajeRepository.guardar(vJorge);
        abrirAlerta(vJorge, evtJorge, jorge, flotaId, ahora.minusSeconds(34), false, null, null);

        // Diana Morales (idx 2) y Andres Pineda (idx 0): PRECAUCION.
        viajeEnCursoGuardado(c.get(2).getUsuarioId(), ahora.minusHours(2).minusMinutes(5),
                ahora.minusSeconds(6), 4.8133, -74.3542, NivelAlerta.PRECAUCION, 64.0);
        viajeEnCursoGuardado(c.get(0).getUsuarioId(), ahora.minusHours(4).minusMinutes(40),
                ahora.minusSeconds(3), 4.4389, -75.2322, NivelAlerta.PRECAUCION, 55.0);

        // Oscar Beltran (idx 6): SIN SEÑAL (ultima senal hace > 2 min).
        viajeEnCursoGuardado(c.get(6).getUsuarioId(), ahora.minusHours(1).minusMinutes(50),
                ahora.minusMinutes(3), 5.5353, -73.3678, NivelAlerta.NORMAL, 70.0);

        // Resto NORMAL hasta 13 en viaje: idx 1,4,5,7,8,9,10,11.
        viajeEnCursoGuardado(c.get(1).getUsuarioId(), ahora.minusMinutes(42), ahora.minusSeconds(2), 4.7110, -74.0721, NivelAlerta.NORMAL, 60.0);
        viajeEnCursoGuardado(c.get(4).getUsuarioId(), ahora.minusHours(1).minusMinutes(15), ahora.minusSeconds(5), 4.9818, -74.0167, NivelAlerta.NORMAL, 58.0);
        viajeEnCursoGuardado(c.get(5).getUsuarioId(), ahora.minusHours(5).minusMinutes(2), ahora.minusSeconds(1), 5.0689, -75.5174, NivelAlerta.NORMAL, 72.0);
        viajeEnCursoGuardado(c.get(7).getUsuarioId(), ahora.minusMinutes(28), ahora.minusSeconds(7), 4.6097, -74.0817, NivelAlerta.NORMAL, 49.0);
        viajeEnCursoGuardado(c.get(12).getUsuarioId(), ahora.minusHours(2).minusMinutes(30), ahora.minusSeconds(9), 4.5389, -75.6722, NivelAlerta.NORMAL, 66.0);
        viajeEnCursoGuardado(c.get(13).getUsuarioId(), ahora.minusMinutes(55), ahora.minusSeconds(4), 5.0321, -74.2110, NivelAlerta.NORMAL, 61.0);
        viajeEnCursoGuardado(c.get(14).getUsuarioId(), ahora.minusHours(3).minusMinutes(20), ahora.minusSeconds(8), 4.8891, -73.9012, NivelAlerta.NORMAL, 69.0);
        viajeEnCursoGuardado(c.get(15).getUsuarioId(), ahora.minusMinutes(12), ahora.minusSeconds(6), 5.2104, -74.7345, NivelAlerta.NORMAL, 52.0);
        viajeEnCursoGuardado(c.get(16).getUsuarioId(), ahora.minusHours(1).minusMinutes(38), ahora.minusSeconds(3), 4.7020, -74.1345, NivelAlerta.NORMAL, 57.0);

        // --- Conductores SIN VIAJE (11): viajes FINALIZADOS previos (idx 8..11 y 17..23) ---
        viajeFinalizado(c.get(8).getUsuarioId(), ahora.minusHours(4), ahora.minusHours(2));   // Natalia (Hoy)
        viajeFinalizado(c.get(9).getUsuarioId(), ahora.minusHours(7), ahora.minusHours(5));   // Fabian (Hoy)
        viajeFinalizado(c.get(10).getUsuarioId(), ahora.minusDays(1).minusHours(3), ahora.minusDays(1).minusHours(1)); // Sandra (Ayer)
        viajeFinalizado(c.get(11).getUsuarioId(), ahora.minusDays(1).minusHours(6), ahora.minusDays(1).minusHours(4)); // Hernan (Ayer)
        for (int i = 17; i < c.size(); i++) {
            viajeFinalizado(c.get(i).getUsuarioId(),
                    ahora.minusDays(2).minusHours(i % 5 + 1), ahora.minusDays(2).minusHours(i % 5));
        }

        // --- Historial de incidentes del turno (eventos de fatiga adicionales) ---
        // Oscar Beltran: microsueno NO respondido, con alerta ATENDIDA por Laura Gomez.
        // Se construye un viaje FINALIZADO que ya incluye el evento (sin releer de BD).
        PerfilConductor oscar = c.get(6);
        UUID viajeOscarId = UUID.randomUUID();
        EventoFatiga evtOscar = new EventoFatiga(UUID.randomUUID(), viajeOscarId, TipoEvento.MICROSUENO,
                ahora.minusHours(5), new Ubicacion(5.0221, -73.8817), 2.8, false, null, null);
        Viaje vOscar = new Viaje(viajeOscarId, oscar.getUsuarioId(), null, null,
                EstadoViaje.FINALIZADO, NivelAlerta.CRITICO, ahora.minusHours(6), ahora.minusHours(4),
                new Ubicacion(5.0221, -73.8817), null, ahora.minusHours(4), 0.0,
                List.of(evtOscar));
        viajeRepository.guardar(vOscar);
        AlertaCritica alertaOscar = abrirAlerta(vOscar, evtOscar, oscar, flotaId,
                ahora.minusHours(5), true,
                "Llamé al conductor. Se detuvo en la estación de servicio de Gachancipá a descansar 20 minutos antes de seguir.",
                ahora.minusHours(5).plusMinutes(3));

        // Eventos del historial del turno SIN alerta (atencion = NO_REQUIERE):
        // microsuenos respondidos y eventos de rostro no visible. Van sobre viajes
        // finalizados independientes para poblar la pantalla "Historial de incidentes".
        eventoHistorial(c.get(0), TipoEvento.MICROSUENO, ahora.minusMinutes(90), 1.8, true, 3.0, 4.4389, -75.2322);      // Andres Pineda respondio 3s
        eventoHistorial(c.get(2), TipoEvento.ROSTRO_NO_VISIBLE, ahora.minusMinutes(115), 9.0, null, null, 4.8133, -74.3542); // Diana Morales rostro 9s
        eventoHistorial(c.get(5), TipoEvento.MICROSUENO, ahora.minusMinutes(130), 1.6, true, 1.0, 5.0689, -75.5174);     // Miguel A. Torres respondio 1s
        eventoHistorial(c.get(7), TipoEvento.ROSTRO_NO_VISIBLE, ahora.minusMinutes(155), 12.0, null, null, 4.6097, -74.0817); // Paola Castro rostro 12s
        eventoHistorial(c.get(2), TipoEvento.MICROSUENO, ahora.minusMinutes(200), 1.9, true, 2.0, 4.8133, -74.3542);     // Diana Morales respondio 2s
        eventoHistorial(c.get(4), TipoEvento.ROSTRO_NO_VISIBLE, ahora.minusMinutes(220), 7.0, null, null, 4.9818, -74.0167); // Luisa F. Rojas rostro 7s
        eventoHistorial(c.get(1), TipoEvento.MICROSUENO, ahora.minusMinutes(240), 1.7, true, 2.0, 4.7110, -74.0721);     // Carlos Rodriguez respondio 2s
        eventoHistorial(c.get(12), TipoEvento.MICROSUENO, ahora.minusMinutes(260), 1.6, true, 4.0, 4.5389, -75.6722);    // respondio 4s
        eventoHistorial(c.get(13), TipoEvento.ROSTRO_NO_VISIBLE, ahora.minusMinutes(280), 6.0, null, null, 5.0321, -74.2110);
        eventoHistorial(c.get(14), TipoEvento.MICROSUENO, ahora.minusMinutes(300), 1.8, true, 3.0, 4.8891, -73.9012);
        eventoHistorial(c.get(0), TipoEvento.ROSTRO_NO_VISIBLE, ahora.minusMinutes(320), 8.0, null, null, 4.4389, -75.2322);
        eventoHistorial(c.get(5), TipoEvento.MICROSUENO, ahora.minusMinutes(340), 1.5, true, 1.0, 5.0689, -75.5174);

        log.info("Seed: viajes y eventos sembrados (alerta abierta de Jorge y alerta atendida de Óscar id={}).",
                alertaOscar.getId());
    }

    /**
     * Crea un viaje finalizado con un unico evento de fatiga para el historial.
     * Si es un microsueno respondido o un rostro-no-visible, no abre alerta.
     */
    private void eventoHistorial(PerfilConductor conductor, TipoEvento tipo, LocalDateTime cuando,
                                 double duracion, Boolean respondio, Double tiempoRespuesta,
                                 double lat, double lon) {
        UUID viajeId = UUID.randomUUID();
        EventoFatiga evento = new EventoFatiga(UUID.randomUUID(), viajeId, tipo, cuando,
                new Ubicacion(lat, lon), duracion, respondio, tiempoRespuesta, null);
        Viaje viaje = new Viaje(viajeId, conductor.getUsuarioId(), null, null,
                EstadoViaje.FINALIZADO, NivelAlerta.NORMAL, cuando.minusMinutes(30), cuando.plusMinutes(20),
                new Ubicacion(lat, lon), null, cuando.plusMinutes(20), 0.0, List.of(evento));
        viajeRepository.guardar(viaje);
    }

    private Viaje viajeEnCurso(UUID conductorId, LocalDateTime inicio, LocalDateTime ultimaSenal,
                               double lat, double lon, NivelAlerta nivel, double velocidad) {
        return new Viaje(UUID.randomUUID(), conductorId, null, null, EstadoViaje.EN_CURSO,
                nivel, inicio, null, new Ubicacion(lat, lon), null, ultimaSenal, velocidad,
                new ArrayList<>());
    }

    private void viajeEnCursoGuardado(UUID conductorId, LocalDateTime inicio, LocalDateTime ultimaSenal,
                                      double lat, double lon, NivelAlerta nivel, double velocidad) {
        viajeRepository.guardar(viajeEnCurso(conductorId, inicio, ultimaSenal, lat, lon, nivel, velocidad));
    }

    private Viaje viajeFinalizado(UUID conductorId, LocalDateTime inicio, LocalDateTime fin) {
        return viajeRepository.guardar(new Viaje(UUID.randomUUID(), conductorId, null, null,
                EstadoViaje.FINALIZADO, NivelAlerta.NORMAL, inicio, fin,
                new Ubicacion(4.7110, -74.0721), null, fin, 0.0, new ArrayList<>()));
    }

    private AlertaCritica abrirAlerta(Viaje viaje, EventoFatiga evento, PerfilConductor conductor,
                                      UUID flotaId, LocalDateTime apertura, boolean atendida,
                                      String notas, LocalDateTime fechaAtencion) {
        AlertaCritica alerta = new AlertaCritica(
                UUID.randomUUID(), viaje.getId(), conductor.getUsuarioId(), flotaId,
                evento.getId(), evento.getTipoEvento(), evento.getDuracionSegundos(),
                evento.getFechaHora(), apertura,
                atendida ? fechaAtencion : null,
                evento.getUbicacion(), atendida,
                null, atendida ? "Laura Gómez" : null, atendida ? notas : null);
        return alertaRepository.guardar(alerta);
    }

    private static String capitalizar(String s) {
        if (s == null || s.isBlank()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }

    private static String ultimos4(String documento) {
        String d = documento == null ? "" : documento.replaceAll("\\D", "");
        return d.length() <= 4 ? d : d.substring(d.length() - 4);
    }
}
