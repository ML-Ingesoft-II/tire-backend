import { AlertaEntradaDto, ResultadoProcesamiento } from "../../../dominio/tipos";

/**
 * Puerto de Entrada (Primary / Driving Port)
 * Expone el caso de uso principal de procesamiento y escalamiento de alertas de cabina.
 */
export interface ProcesarAlerta {
  /**
   * Recibe y procesa una alerta emitida por la cabina del vehículo.
   * @param alerta Datos del evento de fatiga detectado.
   * @returns Resultado detallado de la auditoría y escalamiento.
   */
  procesar(alerta: AlertaEntradaDto): Promise<ResultadoProcesamiento>;
}
