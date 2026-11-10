import { AccionNotificacion, CanalNotificacion, ContextoConductor } from "../tipos";
import { Incidente } from "../incidente/Incidente";
import { PoliticaEscalamiento } from "./PoliticaEscalamiento";

/**
 * Patrón GoF: Strategy - Estrategia Concreta
 * 
 * Implementa el escalamiento para conductores particulares que viajan solos.
 * Notifica vía SMS a sus contactos de emergencia con las coordenadas del evento
 * o indicación de pérdida de cobertura/túnel.
 */
export class PoliticaParticularSolo implements PoliticaEscalamiento {
  public determinarAcciones(
    incidente: Incidente,
    contexto: ContextoConductor
  ): AccionNotificacion[] {
    const acciones: AccionNotificacion[] = [];

    const ubicacionStr = incidente.ubicacion
      ? `Ubicación: https://maps.google.com/?q=${incidente.ubicacion.latitud},${incidente.ubicacion.longitud}`
      : "Ubicación GPS: No disponible (posible túnel o zona sin cobertura)";

    const mensaje = `[TIRE ALERTA CRÍTICA] Se ha detectado un episodio de somnolencia (${incidente.tipoEvento}) en el conductor ${contexto.nombre}. ${ubicacionStr}. Por favor intente comunicarse de inmediato con él para verificar su seguridad.`;

    const contactos = contexto.contactosEmergencia ?? [];

    if (contactos.length > 0) {
      for (const contacto of contactos) {
        acciones.push({
          canal: CanalNotificacion.SMS,
          destinatario: contacto.telefono,
          mensaje,
          metadatos: {
            nombreContacto: contacto.nombre,
            relacion: contacto.relacion ?? "Contacto de emergencia",
            incidenteId: incidente.id,
            conductorId: contexto.conductorId
          }
        });
      }
    } else if (contexto.telefono) {
      // Fallback si no tiene contactos de emergencia registrados
      acciones.push({
        canal: CanalNotificacion.SMS,
        destinatario: contexto.telefono,
        mensaje: `[TIRE ALERTA CABINA] Se ha detectado fatiga crítica (${incidente.tipoEvento}). Deténgase en un lugar seguro inmediatamente.`,
        metadatos: {
          incidenteId: incidente.id,
          conductorId: contexto.conductorId
        }
      });
    }

    return acciones;
  }
}
