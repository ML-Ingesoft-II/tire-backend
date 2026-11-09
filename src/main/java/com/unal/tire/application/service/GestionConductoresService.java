package com.unal.tire.application.service;

import com.unal.tire.application.port.in.ComandoActualizarConductor;
import com.unal.tire.application.port.in.ComandoCrearConductor;
import com.unal.tire.application.port.in.GestionConductoresUseCase;
import com.unal.tire.application.port.out.ConductorRepositoryPort;
import com.unal.tire.domain.exception.ConflictoEstadoException;
import com.unal.tire.domain.exception.RecursoNoEncontradoException;
import com.unal.tire.domain.model.Conductor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de gestion de conductores (alta, consulta, actualizacion y
 * desactivacion logica).
 */
@Service
public class GestionConductoresService implements GestionConductoresUseCase {

    private final ConductorRepositoryPort conductorRepository;

    public GestionConductoresService(ConductorRepositoryPort conductorRepository) {
        this.conductorRepository = conductorRepository;
    }

    @Override
    @Transactional
    public Conductor crear(ComandoCrearConductor comando) {
        if (conductorRepository.existePorDocumento(comando.documento())) {
            throw new ConflictoEstadoException("Ya existe un conductor con ese documento");
        }
        if (conductorRepository.existePorCorreo(comando.correo())) {
            throw new ConflictoEstadoException("Ya existe un conductor con ese correo");
        }

        Conductor conductor = new Conductor(
                UUID.randomUUID(),
                comando.nombre(),
                comando.documento(),
                comando.correo(),
                comando.telefono(),
                comando.flotaId(),
                true,
                LocalDateTime.now());

        return conductorRepository.guardar(conductor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conductor> listarTodos() {
        return conductorRepository.listarTodos();
    }

    @Override
    @Transactional(readOnly = true)
    public Conductor obtenerPorId(UUID id) {
        return conductorRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Conductor no encontrado"));
    }

    @Override
    @Transactional
    public Conductor actualizar(UUID id, ComandoActualizarConductor comando) {
        Conductor conductor = obtenerPorId(id);
        conductor.actualizarDatos(comando.nombre(), comando.telefono(), comando.activo());
        return conductorRepository.guardar(conductor);
    }

    @Override
    @Transactional
    public Conductor desactivar(UUID id) {
        Conductor conductor = obtenerPorId(id);
        conductor.desactivar();
        return conductorRepository.guardar(conductor);
    }
}
