import { AccionNotificacion, CanalNotificacion, ContextoConductor } from "../tipos";
import { Incidente } from "../incidente/Incidente";
import { PoliticaEscalamiento } from "./PoliticaEscalamiento";

/**
 * Patrón GoF: Strategy - Estrategia Concreta
 * 
 * Implementa el escalamiento para conductores de flotas de carga crítica (B2B).
 * Despacha un evento hacia el Panel/Dashboard Web del operador o despachador de flota
 * activando los protocolos logísticos de parada obligatoria y verificación telemática.
 */
export class PoliticaFlota implements PoliticaEscalamiento {
  public determinarAcciones(
    incidente: Incidente,
    contexto: ContextoConductor
  ): AccionNotificacion[] {
    const acciones: AccionNotificacion[] = [];

    const canalDashboard = contexto.flota?.canalDashboard ?? `despacho-${contexto.flota?.empresaId ?? "general"}`;
    const despachadorId = contexto.flota?.despachadorId ?? "CENTRO_MONITOREO_FLOTA";

    const coordsStr = incidente.ubicacion
      ? `Lat: ${incidente.ubicacion.latitud}, Lng: ${incidente.ubicacion.longitud}`
      : "GPS: Cobertura perdida / Túnel";

    const mensaje = `[TIRE B2B FLOTA] Alerta crítica de fatiga (${incidente.tipoEvento}). Vehículo: ${contexto.flota?.placaVehiculo ?? "S/P"} | Conductor: ${contexto.nombre} (ID: ${contexto.conductorId}) | Ubicación: ${coordsStr} | Nivel: CRÍTICO. Activar protocolo de parada segura.`;

    acciones.push({
      canal: CanalNotificacion.DASHBOARD_WEB,
      destinatario: canalDashboard,
      mensaje,
      metadatos: {
        tipoAlerta: "FATIGA_CRITICA_FLOTA",
        empresaId: contexto.flota?.empresaId,
        despachadorId,
        placaVehiculo: contexto.flota?.placaVehiculo,
        incidenteId: incidente.id,
        conductorId: contexto.conductorId,
        tipoEvento: incidente.tipoEvento,
        nivelRiesgo: incidente.obtenerNivelRiesgo(),
        timestamp: incidente.timestamp.toISOString(),
        coordenadas: incidente.ubicacion
      }
    });

    return acciones;
  }
}
