import { AccionNotificacion, CanalNotificacion, ResultadoEnvio } from "../../../dominio/tipos";
import { Notificador } from "../../../aplicacion/puertos/salida/Notificador";

/**
 * Patrón de Diseño GoF: Decorator (Estructural)
 * 
 * Envuelve cualquier implementación de Notificador agregando resiliencia frente
 * a fallas transitorias de red (timeouts, micro-cortes en carretera, intermitencia en pasarelas)
 * mediante reintentos exponenciales configurables.
 * 
 * Cumple con SRP (aislando la lógica de reintento de los adaptadores de canal)
 * y OCP (permitiendo incorporar reintentos a cualquier adaptador presente o futuro
 * sin modificar una sola línea de los mismos).
 */
export class NotificadorConReintento implements Notificador {
  public constructor(
    private readonly notificadorDecorado: Notificador,
    private readonly maxReintentos: number = 3,
    private readonly retrasoBaseMs: number = 20
  ) {
    if (maxReintentos < 1) {
      throw new Error("El número máximo de reintentos debe ser al menos 1.");
    }
  }

  public soporta(canal: CanalNotificacion): boolean {
    return this.notificadorDecorado.soporta(canal);
  }

  public async enviar(accion: AccionNotificacion): Promise<ResultadoEnvio> {
    let intentosRealizados = 0;
    let ultimoError: string | undefined;

    while (intentosRealizados < this.maxReintentos) {
      intentosRealizados++;
      try {
        const resultado = await this.notificadorDecorado.enviar(accion);

        if (resultado.exitoso) {
          return {
            ...resultado,
            reintentos: intentosRealizados - 1
          };
        }

        ultimoError = resultado.error ?? "Fallo desconocido en el adaptador decorado.";
      } catch (error) {
        ultimoError = error instanceof Error ? error.message : String(error);
      }

      // Si quedan intentos por realizar, se aplica backoff antes del siguiente intento
      if (intentosRealizados < this.maxReintentos) {
        const tiempoEspera = this.retrasoBaseMs * Math.pow(2, intentosRealizados - 1);
        await this.esperar(tiempoEspera);
      }
    }

    return {
      exitoso: false,
      canal: accion.canal,
      destinatario: accion.destinatario,
      timestamp: new Date(),
      error: `Agotados ${this.maxReintentos} intentos. Último error: ${ultimoError}`,
      reintentos: intentosRealizados - 1
    };
  }

  private esperar(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}
