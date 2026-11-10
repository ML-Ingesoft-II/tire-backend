/**
 * Simulación de API Externa ("Adaptee" en el patrón Adapter): Proveedor de Mensajería SMS (Twilio / Sinch)
 * 
 * Modela la interfaz propietaria e incompatible del proveedor SMS:
 * ProveedorSmsFalso.sendMessage(to, text).
 */
export class ProveedorSmsFalso {
  private fallasRestantes: number = 0;
  private readonly mensajesEnviados: Array<{
    destinatario: string;
    contenido: string;
    fecha: Date;
  }> = [];

  public configurarFallasTransitorias(cantidad: number): void {
    this.fallasRestantes = Math.max(0, cantidad);
  }

  /**
   * Método del proveedor simulado (firma incompatible con el puerto del dominio).
   */
  public async sendMessage(
    destinatario: string,
    contenido: string
  ): Promise<{ sid: string; status: string }> {
    if (this.fallasRestantes > 0) {
      this.fallasRestantes--;
      throw new Error("SMS_GATEWAY_UNAVAILABLE: Falla de comunicación con el gateway de telecomunicaciones.");
    }

    if (!destinatario || destinatario.trim().length === 0) {
      throw new Error("SMS_INVALID_RECIPIENT: Número de teléfono destino requerido.");
    }

    const sid = `SM${Date.now()}${Math.random().toString(36).substring(2, 6)}`;
    this.mensajesEnviados.push({
      destinatario,
      contenido,
      fecha: new Date()
    });

    return {
      sid,
      status: "queued"
    };
  }

  public obtenerHistorial(): ReadonlyArray<{
    destinatario: string;
    contenido: string;
    fecha: Date;
  }> {
    return [...this.mensajesEnviados];
  }

  public limpiar(): void {
    this.mensajesEnviados.length = 0;
    this.fallasRestantes = 0;
  }
}
