import { AccionNotificacion, CanalNotificacion, ResultadoEnvio } from "../../../dominio/tipos";
import { Notificador } from "../../../aplicacion/puertos/salida/Notificador";
import { ProveedorSmsFalso } from "../externos/ProveedorSmsFalso";

/**
 * Patrón de Diseño GoF: Adapter (Estructural)
 * 
 * Adapta la API externa de telefonía SMS (ProveedorSmsFalso.sendMessage)
 * al contrato estándar del puerto de dominio (Notificador.enviar).
 * Cumple con el Principio de Inversión de Dependencias (DIP).
 */
export class NotificadorSmsSimulado implements Notificador {
  public constructor(private readonly proveedorSms: ProveedorSmsFalso) {}

  public soporta(canal: CanalNotificacion): boolean {
    return canal === CanalNotificacion.SMS;
  }

  public async enviar(accion: AccionNotificacion): Promise<ResultadoEnvio> {
    try {
      await this.proveedorSms.sendMessage(accion.destinatario, accion.mensaje);

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
}
