import { AccionNotificacion, CanalNotificacion, ContextoConductor } from "../tipos";
import { Incidente } from "../incidente/Incidente";
import { PoliticaEscalamiento } from "./PoliticaEscalamiento";

/**
 * Patrón GoF: Strategy - Estrategia Concreta
 * 
 * Implementa el escalamiento para conductores particulares acompañados.
 * Prioriza una notificación PUSH de alta prioridad vía Firebase Cloud Messaging (FCM)
 * al dispositivo del copiloto para una intervención física inmediata en cabina.
 */
export class PoliticaParticularConCopiloto implements PoliticaEscalamiento {
  public determinarAcciones(
    incidente: Incidente,
    contexto: ContextoConductor
  ): AccionNotificacion[] {
    const acciones: AccionNotificacion[] = [];

    if (contexto.copiloto?.tokenFcm) {
      acciones.push({
        canal: CanalNotificacion.PUSH_FCM,
        destinatario: contexto.copiloto.tokenFcm,
        mensaje: `[TIRE SEGURIDAD COPILOTO] ¡Atención ${contexto.copiloto.nombre}! Alerta de fatiga crítica (${incidente.tipoEvento}) en el conductor principal (${contexto.nombre}). Intervenga de inmediato en la cabina.`,
        metadatos: {
          prioridad: "HIGH",
          sonido: "alarma_emergencia.wav",
          vibracion: true,
          incidenteId: incidente.id,
          conductorId: contexto.conductorId,
          copilotoNombre: contexto.copiloto.nombre
        }
      });
    }

    return acciones;
  }
}
