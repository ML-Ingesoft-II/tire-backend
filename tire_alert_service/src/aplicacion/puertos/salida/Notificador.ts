import { AccionNotificacion, CanalNotificacion, ResultadoEnvio } from "../../../dominio/tipos";

/**
 * Puerto de Salida (Secondary / Driven Port)
 * Abstracción unificada para el despacho de notificaciones a través de diferentes canales.
 */
export interface Notificador {
  /**
   * Determina si este adaptador maneja el canal de notificación especificado.
   */
  soporta(canal: CanalNotificacion): boolean;

  /**
   * Despacha una acción de notificación hacia el canal correspondiente.
   * @param accion Datos de la notificación (canal, destinatario, mensaje, metadatos).
   * @returns Resultado del envío con confirmación, timestamp y reintentos.
   */
  enviar(accion: AccionNotificacion): Promise<ResultadoEnvio>;
}
