/**
 * Reglas de negocio puras para la determinación del escalamiento de alertas.
 * Capa de Dominio - Sin dependencias externas ni de infraestructura.
 * 
 * Regla central: Una alerta se escala a entidades externas si:
 *   1. No fue silenciada en cabina (!silenciadoEnCabina), lo que indica pérdida de respuesta o somnolencia profunda.
 *   2. O si fue silenciada, pero el conductor acumula eventos repetitivos mayores o iguales al umbral permitido
 *      (repetidas >= umbral), evidenciando fatiga crónica y desestimación peligrosa de alarmas.
 */

export class ReglasEscalamiento {
  public static readonly UMBRAL_REPETIDAS_DEFAULT: number = 3;

  /**
   * Determina si una alerta debe ser escalada hacia los canales externos.
   * @param silenciadoEnCabina Booleano que indica si el conductor apagó la alarma en cabina.
   * @param eventosRecientes Cantidad de eventos de fatiga registrados en la ventana temporal reciente.
   * @param umbralRepetidas Umbral de tolerancia de eventos repetidos (por defecto 3).
   * @returns true si debe escalarse, false si se considera resuelta en cabina.
   */
  public static debeEscalar(
    silenciadoEnCabina: boolean,
    eventosRecientes: number,
    umbralRepetidas: number = ReglasEscalamiento.UMBRAL_REPETIDAS_DEFAULT
  ): boolean {
    return !silenciadoEnCabina || eventosRecientes >= umbralRepetidas;
  }

  /**
   * Genera el motivo explicativo del escalamiento para trazabilidad y auditoría.
   */
  public static obtenerMotivo(
    silenciadoEnCabina: boolean,
    eventosRecientes: number,
    umbralRepetidas: number = ReglasEscalamiento.UMBRAL_REPETIDAS_DEFAULT
  ): string {
    if (!silenciadoEnCabina) {
      return "Alerta en cabina no fue atendida/silenciada por el conductor (alta sospecha de microsueño o pérdida de consciencia).";
    }
    if (eventosRecientes >= umbralRepetidas) {
      return `Alerta silenciada en cabina, pero se superó el umbral de fatiga acumulada (${eventosRecientes} eventos recientes >= umbral ${umbralRepetidas}).`;
    }
    return "Alerta atendida y silenciada en cabina dentro del margen de tolerancia. No requiere escalamiento externo.";
  }
}
