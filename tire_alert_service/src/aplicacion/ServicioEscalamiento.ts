import {
  AlertaEntradaDto,
  CanalNotificacion,
  ResultadoEnvio,
  ResultadoProcesamiento
} from "../dominio/tipos";
import { ReglasEscalamiento } from "../dominio/ReglasEscalamiento";
import { IncidenteBuilder } from "../dominio/incidente/IncidenteBuilder";
import { Incidente } from "../dominio/incidente/Incidente";
import { ProcesarAlerta } from "./puertos/entrada/ProcesarAlerta";
import { ConsultorPerfil } from "./puertos/salida/ConsultorPerfil";
import { RepositorioIncidentes } from "./puertos/salida/RepositorioIncidentes";
import { Notificador } from "./puertos/salida/Notificador";
import { FabricaPoliticas } from "./FabricaPoliticas";

/**
 * Caso de Uso Central: Servicio de Escalamiento y Orquestación de Alertas
 * 
 * Implementa el puerto de entrada ProcesarAlerta.
 * Coordina la evaluación del contexto del conductor, aplicación de reglas de negocio,
 * transición del ciclo de vida del incidente (patrón State), selección de la estrategia
 * de escalamiento (patrón Strategy a través de Factory Method) y despacho resiliente de
 * notificaciones (patrones Adapter y Decorator).
 * 
 * Cumplimiento de Requerimiento Crítico AC06 (Garantía Transaccional):
 * La persistencia del incidente en el repositorio secundario está encapsulada
 * estrictamente dentro de un bloque try ... finally para asegurar auditoría y trazabilidad
 * forense incluso ante fallos críticos durante el despacho de notificaciones externas.
 */
export class ServicioEscalamiento implements ProcesarAlerta {
  public constructor(
    private readonly consultorPerfil: ConsultorPerfil,
    private readonly repositorio: RepositorioIncidentes,
    private readonly notificadores: Notificador[],
    private readonly fabricaPoliticas: FabricaPoliticas = new FabricaPoliticas(),
    private readonly ventanaMinutosRecientes: number = 15
  ) {}

  public async procesar(alerta: AlertaEntradaDto): Promise<ResultadoProcesamiento> {
    // 1. Obtención y validación del contexto del conductor (Puerto de Salida)
    const contexto = await this.consultorPerfil.obtenerContexto(alerta.conductorId);
    if (!contexto) {
      throw new Error(`Conductor con ID '${alerta.conductorId}' no encontrado en el sistema.`);
    }

    // 2. Consulta de eventos recientes para evaluar fatiga crónica/acumulada
    const eventosRecientes = await this.repositorio.contarRecientes(
      alerta.conductorId,
      this.ventanaMinutosRecientes
    );

    // 3. Construcción del agregado Incidente usando el patrón Builder
    const builder = new IncidenteBuilder()
      .conConductorId(alerta.conductorId)
      .conTipoEvento(alerta.tipoEvento)
      .conSilenciadoEnCabina(alerta.silenciadoEnCabina);

    if (alerta.incidenteId) {
      builder.conId(alerta.incidenteId);
    }
    if (alerta.timestamp) {
      builder.conTimestamp(alerta.timestamp);
    }
    if (alerta.ubicacion) {
      builder.conUbicacion(
        alerta.ubicacion.latitud,
        alerta.ubicacion.longitud,
        alerta.ubicacion.altitud,
        alerta.ubicacion.precisionMetros
      );
    }

    const incidente = builder.construir();

    // 4. Evaluación de la regla de negocio pura
    const debeEscalar = ReglasEscalamiento.debeEscalar(
      incidente.silenciadoEnCabina,
      eventosRecientes
    );
    const motivo = ReglasEscalamiento.obtenerMotivo(
      incidente.silenciadoEnCabina,
      eventosRecientes
    );

    const notificacionesDespachadas: ResultadoEnvio[] = [];
    let persistenciaExitosa = false;

    // AC06: Bloque try ... finally para garantizar persistencia y trazabilidad forense
    try {
      if (debeEscalar) {
        // Transición de estado delegada al patrón State
        incidente.escalar(motivo);

        // Selección de estrategia mediante Factory Method
        const politica = this.fabricaPoliticas.crearPara(contexto);
        const acciones = politica.determinarAcciones(incidente, contexto);

        // Despacho de notificaciones a través de los adaptadores
        for (const accion of acciones) {
          const resultado = await this.despacharNotificacion(accion);
          notificacionesDespachadas.push(resultado);
        }
      }
    } finally {
      // AC06: Persistencia incondicional en repositorio, asegurando bitácora
      await this.repositorio.guardar(incidente);
      persistenciaExitosa = true;
    }

    return {
      incidenteId: incidente.id,
      conductorId: incidente.conductorId,
      escalado: incidente.estaEscalado(),
      estadoFinal: incidente.obtenerNombreEstado(),
      nivelRiesgoFinal: incidente.obtenerNivelRiesgo(),
      motivoEscalamiento: motivo,
      notificacionesDespachadas,
      persistenciaExitosa
    };
  }

  /**
   * Busca el notificador compatible con el canal de la acción y ejecuta el envío.
   */
  private async despacharNotificacion(accion: {
    canal: CanalNotificacion;
    destinatario: string;
    mensaje: string;
    metadatos?: Record<string, unknown>;
  }): Promise<ResultadoEnvio> {
    const notificadorCompatible = this.notificadores.find(n => n.soporta(accion.canal));

    if (!notificadorCompatible) {
      return {
        exitoso: false,
        canal: accion.canal,
        destinatario: accion.destinatario,
        timestamp: new Date(),
        error: `No existe un adaptador de notificación registrado para el canal '${accion.canal}'.`
      };
    }

    return notificadorCompatible.enviar(accion);
  }
}
