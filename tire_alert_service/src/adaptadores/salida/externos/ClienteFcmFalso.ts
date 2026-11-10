/**
 * Simulación de SDK Externo ("Adaptee" en el patrón Adapter): Firebase Cloud Messaging (FCM)
 * 
 * Modela la interfaz propietaria e incompatible del SDK oficial de Google Firebase Admin:
 * ClienteFcmFalso.send(deviceToken, messagePayload).
 */
export class ClienteFcmFalso {
  private fallasRestantes: number = 0;
  private readonly mensajesEnviados: Array<{
    token: string;
    payload: {
      notification: { title: string; body: string };
      data?: Record<string, unknown>;
    };
    fecha: Date;
  }> = [];

  /**
   * Configura una cantidad de fallas de red simuladas consecutivas para pruebas de resiliencia.
   */
  public configurarFallasTransitorias(cantidad: number): void {
    this.fallasRestantes = Math.max(0, cantidad);
  }

  /**
   * Método del SDK simulado (firma incompatible con el puerto del dominio).
   */
  public async send(
    token: string,
    payload: {
      notification: { title: string; body: string };
      data?: Record<string, unknown>;
    }
  ): Promise<{ messageId: string; status: string }> {
    if (this.fallasRestantes > 0) {
      this.fallasRestantes--;
      throw new Error("FCM_NETWORK_TIMEOUT: Conexión interrumpida con los servidores de Google FCM.");
    }

    if (!token || token.trim().length === 0) {
      throw new Error("FCM_INVALID_TOKEN: El token FCM de destino está vacío o es inválido.");
    }

    const messageId = `fcm-msg-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`;
    this.mensajesEnviados.push({
      token,
      payload,
      fecha: new Date()
    });

    return {
      messageId,
      status: "SUCCESS"
    };
  }

  public obtenerHistorial(): ReadonlyArray<{
    token: string;
    payload: {
      notification: { title: string; body: string };
      data?: Record<string, unknown>;
    };
    fecha: Date;
  }> {
    return [...this.mensajesEnviados];
  }

  public limpiar(): void {
    this.mensajesEnviados.length = 0;
    this.fallasRestantes = 0;
  }
}
