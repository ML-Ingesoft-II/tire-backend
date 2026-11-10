import { AccionNotificacion, CanalNotificacion, ResultadoEnvio } from "../../../dominio/tipos";
import { Notificador } from "../../../aplicacion/puertos/salida/Notificador";

/**
 * Patrón GoF: Adapter (Estructural)
 * 
 * Simula el despacho de notificaciones en tiempo real (vía WebSockets / SSE / Event Bus)
 * hacia el Panel de Monitoreo Web de los despachadores de flota B2B.
 */
export class NotificadorDashboardSimulado implements Notificador {
  private readonly eventosTransmitidos: Array<{
    canalDashboard: string;
    mensaje: string;
    metadatos?: Record<string, unknown>;
    timestamp: Date;
  }> = [];

  public soporta(canal: CanalNotificacion): boolean {
    return canal === CanalNotificacion.DASHBOARD_WEB;
  }

  public async enviar(accion: AccionNotificacion): Promise<ResultadoEnvio> {
    try {
      this.eventosTransmitidos.push({
        canalDashboard: accion.destinatario,
        mensaje: accion.mensaje,
        metadatos: accion.metadatos,
        timestamp: new Date()
      });

      return {
        exitoso: true,
        canal: accion.canal,
        destinatario: accion.destinatario,
        timestamp: new Date()
      };
    } catch (error) {
      return {
        exitoso: false,
        canal: accion.canal,
        destinatario: accion.destinatario,
        timestamp: new Date(),
        error: error instanceof Error ? error.message : String(error)
      };
    }
  }

  public obtenerHistorial(): ReadonlyArray<{
    canalDashboard: string;
    mensaje: string;
    metadatos?: Record<string, unknown>;
    timestamp: Date;
  }> {
    return [...this.eventosTransmitidos];
  }

  public limpiar(): void {
    this.eventosTransmitidos.length = 0;
  }
}
