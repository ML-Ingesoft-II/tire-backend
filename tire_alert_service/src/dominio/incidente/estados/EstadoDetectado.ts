import { NivelRiesgo } from "../../tipos";
import type { Incidente } from "../Incidente";
import { EstadoIncidente } from "./EstadoIncidente";
import { EstadoEscalado } from "./EstadoEscalado";
import { EstadoAtendido } from "./EstadoAtendido";

/**
 * Patrón GoF: State - Estado Concreto
 * Representa el estado inicial de un evento de fatiga recién recibido desde la cabina (Edge AI).
 */
export class EstadoDetectado implements EstadoIncidente {
  public readonly nombre: string = "DETECTADO";

  public obtenerNivelRiesgo(): NivelRiesgo {
    return NivelRiesgo.PRECAUCION;
  }

  public escalar(contexto: Incidente, motivo?: string): void {
    contexto.cambiarEstado(
      new EstadoEscalado(),
      motivo ?? "Incidente escalado debido a reglas de negocio de fatiga."
    );
  }

  public atender(contexto: Incidente, motivo?: string): void {
    contexto.cambiarEstado(
      new EstadoAtendido(),
      motivo ?? "Incidente cerrado y atendido directamente en cabina."
    );
  }
}
