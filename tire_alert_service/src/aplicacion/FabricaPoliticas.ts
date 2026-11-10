import { ContextoConductor, TipoPerfilConductor } from "../dominio/tipos";
import { PoliticaEscalamiento } from "../dominio/politicas/PoliticaEscalamiento";
import { PoliticaParticularSolo } from "../dominio/politicas/PoliticaParticularSolo";
import { PoliticaParticularConCopiloto } from "../dominio/politicas/PoliticaParticularConCopiloto";
import { PoliticaFlota } from "../dominio/politicas/PoliticaFlota";

/**
 * Patrón de Diseño GoF: Factory Method (Creacional)
 * 
 * Desacopla la capa de aplicación y el caso de uso ServicioEscalamiento
 * de la instanciación directa (con 'new') de las estrategias concretas de escalamiento.
 * 
 * Cumple con SRP (responsabilidad única de instanciación) y OCP (Abierto/Cerrado),
 * permitiendo añadir nuevos perfiles (ej. TRANSPORTE_PUBLICO) extendiendo la fábrica
 * mediante herencia o mediante su registro dinámico sin modificar código en src/.
 */
export class FabricaPoliticas {
  private static readonly registroGlobal: Map<
    string,
    (contexto: ContextoConductor) => PoliticaEscalamiento
  > = new Map();

  /**
   * Permite registrar dinámicamente nuevas políticas de escalamiento.
   * Mecanismo para extensión sin modificación (Principio Open/Closed - OCP).
   */
  public static registrarPolitica(
    tipoPerfil: string,
    creador: (contexto: ContextoConductor) => PoliticaEscalamiento
  ): void {
    this.registroGlobal.set(tipoPerfil.toUpperCase(), creador);
  }

  /**
   * Limpia el registro global de políticas dinámicas (útil en pruebas).
   */
  public static limpiarRegistro(): void {
    this.registroGlobal.clear();
  }

  /**
   * Crea la estrategia de escalamiento adecuada según el perfil del conductor.
   * @param contexto Contexto del conductor que incluye su tipo de perfil.
   */
  public crearPara(contexto: ContextoConductor): PoliticaEscalamiento {
    const tipoNormalizado = String(contexto.tipoPerfil).toUpperCase();

    // 1. Consulta en registro extensible (extensión dinámica OCP)
    const creadorRegistrado = FabricaPoliticas.registroGlobal.get(tipoNormalizado);
    if (creadorRegistrado) {
      return creadorRegistrado(contexto);
    }

    // 2. Invoca el método hook de fabricación concreta (extensión por herencia)
    return this.crearPoliticaConcreta(tipoNormalizado, contexto);
  }

  /**
   * Método de fabricación hook (Factory Method clásico GoF).
   * Diseñado para ser sobreescrito por subclases si se prefiere extensión por herencia.
   */
  protected crearPoliticaConcreta(
    tipo: string,
    contexto: ContextoConductor
  ): PoliticaEscalamiento {
    switch (tipo) {
      case TipoPerfilConductor.PARTICULAR_SOLO:
        return new PoliticaParticularSolo();

      case TipoPerfilConductor.PARTICULAR_CON_COPILOTO:
        return new PoliticaParticularConCopiloto();

      case TipoPerfilConductor.FLOTA:
        return new PoliticaFlota();

      default:
        throw new Error(
          `No se encontró una política de escalamiento para el tipo de perfil: '${contexto.tipoPerfil}'.`
        );
    }
  }
}
