import { AccionNotificacion, ContextoConductor } from "../tipos";
import { Incidente } from "../incidente/Incidente";

/**
 * Patrón de Diseño GoF: Strategy (Comportamiento)
 * 
 * Interfaz que define la familia de algoritmos para determinar las acciones
 * de notificación y escalamiento según el perfil del conductor y el incidente.
 */
export interface PoliticaEscalamiento {
  /**
   * Determina la lista de acciones de notificación a despachar.
   * @param incidente Agregado del incidente de fatiga evaluado.
   * @param contexto Perfil y datos contextuales del conductor.
   * @returns Lista de acciones con canal, destinatario y mensaje formateado.
   */
  determinarAcciones(incidente: Incidente, contexto: ContextoConductor): AccionNotificacion[];
}
