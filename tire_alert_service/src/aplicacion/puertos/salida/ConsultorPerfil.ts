import { ContextoConductor } from "../../../dominio/tipos";

/**
 * Puerto de Salida (Secondary / Driven Port)
 * Permite a la capa de aplicación consultar la información y perfil contextual del conductor.
 */
export interface ConsultorPerfil {
  /**
   * Obtiene el contexto completo del conductor por su identificador.
   * @param conductorId Identificador único del conductor.
   * @returns El contexto con perfil, contactos y datos de flota, o null si no existe.
   */
  obtenerContexto(conductorId: string): Promise<ContextoConductor | null>;
}
