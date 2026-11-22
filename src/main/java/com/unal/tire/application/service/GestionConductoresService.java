package com.unal.tire.application.service;

import com.unal.tire.application.port.in.ComandoActualizarConductor;
import com.unal.tire.application.port.in.ComandoCrearConductor;
import com.unal.tire.application.port.in.GestionConductoresUseCase;
import com.unal.tire.application.port.out.ConductorRepositoryPort;
import com.unal.tire.application.port.out.ProveedorAutenticacionPort;
import com.unal.tire.application.port.out.UsuarioRepositoryPort;
import com.unal.tire.application.port.out.ViajeRepositoryPort;
import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.model.PerfilConductor;
import com.unal.tire.domain.model.Usuario;
import com.unal.tire.domain.valueobject.RolUsuario;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de gestion de conductores de la flota (HU-05, HU-06).
 *
 * <p>Crea en una transaccion el {@link Usuario} (rol CONDUCTOR) y el
 * {@link PerfilConductor} con el mismo UUID. Todas las consultas se limitan a la
 * flota del despachador.</p>
 */
@Service
public class GestionConductoresService implements GestionConductoresUseCase {

    private final ConductorRepositoryPort conductorRepository;
    private final UsuarioRepositoryPort usuarioRepository;
    private final ProveedorAutenticacionPort proveedorAutenticacion;
    private final ViajeRepositoryPort viajeRepository;

    public GestionConductoresService(
            ConductorRepositoryPort conductorRepository,
            UsuarioRepositoryPort usuarioRepository,
            ProveedorAutenticacionPort proveedorAutenticacion,
            ViajeRepositoryPort viajeRepository) {
        this.conductorRepository = conductorRepository;
        this.usuarioRepository = usuarioRepository;
        this.proveedorAutenticacion = proveedorAutenticacion;
        this.viajeRepository = viajeRepository;
    }

    @Override
    @Transactional
    public ConductorVista crear(ComandoCrearConductor comando) {
        if (conductorRepository.existePorDocumento(comando.numeroDocumento())) {
            throw new ConflictoEstadoException("Ya existe un conductor con ese documento");
        }
        if (usuarioRepository.existePorEmail(comando.correo())) {
            throw new ConflictoEstadoException("Ya existe un conductor con ese correo");
        }

        UUID id = UUID.randomUUID();
        String passwordInicial = generarPasswordInicial(comando.apellidos(), comando.numeroDocumento());
        String passwordHash = proveedorAutenticacion.hashearContrasena(passwordInicial);

        Usuario usuario = new Usuario(
                id,
                comando.correo(),
                passwordHash,
                (comando.nombres() + " " + comando.apellidos()).trim(),
                RolUsuario.CONDUCTOR,
                comando.flotaId(),
                true,
                true);
        usuarioRepository.guardar(usuario);

        PerfilConductor perfil = new PerfilConductor(
                id,
                comando.nombres(),
                comando.apellidos(),
                comando.tipoDocumento(),
                comando.numeroDocumento(),
                comando.telefono(),
                comando.placaVehiculo(),
                comando.flotaId(),
                true,
                LocalDateTime.now());
        PerfilConductor guardado = conductorRepository.guardar(perfil);

        return new ConductorVista(guardado, comando.correo(), false);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaConductores listar(UUID flotaId, String q, Boolean activo, int page, int size) {
        String filtro = q != null ? q.trim().toLowerCase() : null;

        List<ConductorVista> todos = conductorRepository.listarPorFlota(flotaId).stream()
                .filter(p -> activo == null || p.isActivo() == activo)
                .filter(p -> filtro == null || filtro.isBlank() || coincide(p, filtro))
                .sorted(Comparator.comparing(PerfilConductor::getNombreCompleto,
                        String.CASE_INSENSITIVE_ORDER))
                .map(this::aVista)
                .toList();

        long totalElementos = todos.size();
        int totalPaginas = size > 0 ? (int) Math.ceil((double) totalElementos / size) : 0;
        int desde = Math.min(page * size, todos.size());
        int hasta = Math.min(desde + size, todos.size());
        List<ConductorVista> contenido = todos.subList(desde, hasta);

        return new PaginaConductores(page, size, totalElementos, totalPaginas, contenido);
    }

    @Override
    @Transactional(readOnly = true)
    public ConductorVista obtenerPorId(UUID flotaId, UUID conductorId) {
        return aVista(cargarEnFlota(flotaId, conductorId));
    }

    @Override
    @Transactional
    public ConductorVista actualizar(UUID flotaId, UUID conductorId, ComandoActualizarConductor comando) {
        PerfilConductor perfil = cargarEnFlota(flotaId, conductorId);

        // Desactivacion via PATCH sigue la misma regla que DELETE.
        if (Boolean.FALSE.equals(comando.activo()) && perfil.isActivo()
                && viajeRepository.existeViajeEnCursoParaConductor(conductorId)) {
            throw new ConflictoEstadoException("Este conductor tiene un viaje en curso");
        }

        if (comando.numeroDocumento() != null
                && !comando.numeroDocumento().equals(perfil.getNumeroDocumento())
                && conductorRepository.existePorDocumento(comando.numeroDocumento())) {
            throw new ConflictoEstadoException("Ya existe un conductor con ese documento");
        }

        perfil.actualizarDatos(
                comando.nombres(),
                comando.apellidos(),
                comando.tipoDocumento(),
                comando.numeroDocumento(),
                comando.telefono(),
                comando.placaVehiculo(),
                comando.activo());
        PerfilConductor guardado = conductorRepository.guardar(perfil);

        String correo = actualizarUsuario(conductorId, comando, guardado);

        return new ConductorVista(guardado, correo,
                viajeRepository.existeViajeEnCursoParaConductor(conductorId));
    }

    @Override
    @Transactional
    public ConductorVista desactivar(UUID flotaId, UUID conductorId) {
        PerfilConductor perfil = cargarEnFlota(flotaId, conductorId);

        if (viajeRepository.existeViajeEnCursoParaConductor(conductorId)) {
            throw new ConflictoEstadoException("Este conductor tiene un viaje en curso");
        }

        perfil.desactivar();
        PerfilConductor guardado = conductorRepository.guardar(perfil);

        usuarioRepository.buscarPorId(conductorId).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.guardar(u);
        });

        return aVista(guardado);
    }

    // ----- Helpers -----

    private PerfilConductor cargarEnFlota(UUID flotaId, UUID conductorId) {
        PerfilConductor perfil = conductorRepository.buscarPorId(conductorId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Conductor no encontrado"));
        // Alcance por flota: un conductor de otra flota responde 404.
        if (flotaId == null || !flotaId.equals(perfil.getFlotaId())) {
            throw new RecursoNoEncontradoException("Conductor no encontrado");
        }
        return perfil;
    }

    private String actualizarUsuario(UUID conductorId, ComandoActualizarConductor comando,
                                     PerfilConductor perfil) {
        Usuario usuario = usuarioRepository.buscarPorId(conductorId).orElse(null);
        if (usuario == null) {
            return comando.correo();
        }
        if (comando.correo() != null) {
            usuario.setEmail(comando.correo());
        }
        if (comando.nombres() != null || comando.apellidos() != null) {
            usuario.setNombre(perfil.getNombreCompleto());
        }
        if (comando.activo() != null) {
            usuario.setActivo(comando.activo());
        }
        usuarioRepository.guardar(usuario);
        return usuario.getEmail();
    }

    private ConductorVista aVista(PerfilConductor perfil) {
        String correo = usuarioRepository.buscarPorId(perfil.getUsuarioId())
                .map(Usuario::getEmail)
                .orElse(null);
        boolean enCurso = viajeRepository.existeViajeEnCursoParaConductor(perfil.getUsuarioId());
        return new ConductorVista(perfil, correo, enCurso);
    }

    private boolean coincide(PerfilConductor p, String filtro) {
        String correo = usuarioRepository.buscarPorId(p.getUsuarioId())
                .map(Usuario::getEmail).orElse("");
        return contiene(p.getNombreCompleto(), filtro)
                || contiene(p.getNumeroDocumento(), filtro)
                || contiene(p.getPlacaVehiculo(), filtro)
                || contiene(correo, filtro);
    }

    private boolean contiene(String valor, String filtro) {
        return valor != null && valor.toLowerCase().contains(filtro);
    }

    /**
     * Contrasena inicial: primer apellido capitalizado (sin espacios) + ultimos
     * 4 digitos del documento. Ejemplo: "Quintero8392".
     */
    static String generarPasswordInicial(String apellidos, String numeroDocumento) {
        String primerApellido = apellidos == null ? "" : apellidos.trim().split("\\s+")[0];
        String capitalizado = primerApellido.isEmpty()
                ? ""
                : Character.toUpperCase(primerApellido.charAt(0)) + primerApellido.substring(1).toLowerCase();

        String soloDigitos = numeroDocumento == null ? "" : numeroDocumento.replaceAll("\\D", "");
        String ultimos4 = soloDigitos.length() >= 4
                ? soloDigitos.substring(soloDigitos.length() - 4)
                : soloDigitos;

        return capitalizado + ultimos4;
    }
}
