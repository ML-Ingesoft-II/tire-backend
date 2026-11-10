import { NivelRiesgo } from "../../tipos";
import type { Incidente } from "../Incidente";

/**
 * Patrón de Diseño GoF: State (Comportamiento)
 * Interfaz que define el contrato de comportamiento para los diferentes estados
 * del ciclo de vida de un Incidente de fatiga.
 */
export interface EstadoIncidente {
  readonly nombre: string;

  /**
   * Retorna el nivel de riesgo intrínseco asociado a este estado del ciclo de vida.
   */
  obtenerNivelRiesgo(): NivelRiesgo;

  /**
   * Intenta realizar la transición hacia el estado Escalado.
   * @param contexto Agregado Incidente cuyo estado se modifica.
   * @param motivo Justificación del escalamiento.
   */
  escalar(contexto: Incidente, motivo?: string): void;

  /**
   * Intenta realizar la transición hacia el estado Atendido (cierre del incidente).
   * @param contexto Agregado Incidente.
   * @param motivo Justificación de la resolución o cierre.
   */
  atender(contexto: Incidente, motivo?: string): void;
}
