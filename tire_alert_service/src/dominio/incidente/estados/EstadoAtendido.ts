import { NivelRiesgo } from "../../tipos";
import type { Incidente } from "../Incidente";
import { EstadoIncidente } from "./EstadoIncidente";

/**
 * Patrón GoF: State - Estado Concreto (Estado Terminal)
 * Representa un incidente resuelto, atendido y archivado.
 */
export class EstadoAtendido implements EstadoIncidente {
  public readonly nombre: string = "ATENDIDO";

  public obtenerNivelRiesgo(): NivelRiesgo {
    return NivelRiesgo.NORMAL;
  }

  public escalar(contexto: Incidente, motivo?: string): void {
    throw new Error(
      `Operación inválida: No se puede escalar un incidente que ya fue resuelto y se encuentra en estado '${this.nombre}'.`
    );
  }

  public atender(contexto: Incidente, motivo?: string): void {
    // Ya se encuentra atendido (operación idempotente)
  }
}
