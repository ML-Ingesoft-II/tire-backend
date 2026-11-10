import { Incidente } from "../../../dominio/incidente/Incidente";

/**
 * Puerto de Salida (Secondary / Driven Port)
 * Define el contrato de persistencia y auditoría de incidentes de fatiga.
 */
export interface RepositorioIncidentes {
  /**
   * Persiste un incidente en el almacenamiento secundario.
   * Crucial para auditoría y requerimiento AC06 (garantía de persistencia).
   */
  guardar(incidente: Incidente): Promise<void>;

  /**
   * Cuenta cuántos eventos ha registrado un conductor en una ventana de tiempo reciente.
   * @param conductorId Identificador del conductor.
   * @param minutosVentana Minutos previos hacia atrás a consultar (ej. 15 minutos).
   */
  contarRecientes(conductorId: string, minutosVentana: number): Promise<number>;

  /**
   * Busca un incidente por su identificador único.
   */
  buscarPorId(id: string): Promise<Incidente | null>;

  /**
   * Lista todos los incidentes registrados para auditoría o monitoreo.
   */
  listarTodos(): Promise<Incidente[]>;

  /**
   * Limpia los datos almacenados (útil en pruebas).
   */
  limpiar?(): Promise<void>;
}
