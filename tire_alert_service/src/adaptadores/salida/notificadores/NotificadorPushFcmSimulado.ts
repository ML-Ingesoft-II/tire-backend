import { AccionNotificacion, CanalNotificacion, ResultadoEnvio } from "../../../dominio/tipos";
import { Notificador } from "../../../aplicacion/puertos/salida/Notificador";
import { ClienteFcmFalso } from "../externos/ClienteFcmFalso";

/**
 * Patrón de Diseño GoF: Adapter (Estructural)
 * 
 * Adapta la interfaz propietaria de Firebase Cloud Messaging (ClienteFcmFalso.send)
 * al puerto estándar del dominio (Notificador.enviar).
 * Cumple con el Principio de Inversión de Dependencias (DIP).
 */
export class NotificadorPushFcmSimulado implements Notificador {
  public constructor(private readonly clienteFcm: ClienteFcmFalso) {}

  public soporta(canal: CanalNotificacion): boolean {
    return canal === CanalNotificacion.PUSH_FCM;
  }

  public async enviar(accion: AccionNotificacion): Promise<ResultadoEnvio> {
    try {
      await this.clienteFcm.send(accion.destinatario, {
        notification: {
          title: "¡ALERTA CRÍTICA TIRE - CABINA!",
          body: accion.mensaje
        },
        data: accion.metadatos
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
}
