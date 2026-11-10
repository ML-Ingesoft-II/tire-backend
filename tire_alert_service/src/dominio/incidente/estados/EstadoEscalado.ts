import { NivelRiesgo } from "../../tipos";
import type { Incidente } from "../Incidente";
import { EstadoIncidente } from "./EstadoIncidente";
import { EstadoAtendido } from "./EstadoAtendido";

/**
 * Patrón GoF: State - Estado Concreto
 * Representa un incidente activo que superó los umbrales de cabina y ha sido
 * escalado a contactos de emergencia, copiloto o despachador de flota.
 */
export class EstadoEscalado implements EstadoIncidente {
  public readonly nombre: string = "ESCALADO";

  public obtenerNivelRiesgo(): NivelRiesgo {
    return NivelRiesgo.CRITICO;
  }

  public escalar(contexto: Incidente, motivo?: string): void {
    // Ya se encuentra en estado escalado (operación idempotente)
  }

  public atender(contexto: Incidente, motivo?: string): void {
    contexto.cambiarEstado(
      new EstadoAtendido(),
      motivo ?? "Incidente escalado atendido por operador o tripulación externa."
    );
  }
}
