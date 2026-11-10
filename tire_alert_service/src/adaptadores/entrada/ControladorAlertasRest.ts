import { AlertaEntradaDto, PeticionHttp, RespuestaHttp, TipoEvento } from "../../dominio/tipos";
import { ProcesarAlerta } from "../../aplicacion/puertos/entrada/ProcesarAlerta";

/**
 * Adaptador de Entrada: Controlador HTTP REST de Alertas de Cabina
 * 
 * Expone la API REST para la recepción de eventos telemáticos desde la app móvil / Edge AI.
 * Valida la estructura sintáctica y reglas de formato del payload antes de invocar
 * el puerto de entrada ProcesarAlerta de la capa de aplicación.
 */
export class ControladorAlertasRest {
  public constructor(private readonly casoDeUso: ProcesarAlerta) {}

  /**
   * Procesa una petición HTTP REST entrante.
   * Retorna una respuesta normalizada (status code y body).
   */
  public async procesarAlerta(peticion: PeticionHttp): Promise<RespuestaHttp> {
    const rawBody = peticion.body as Record<string, unknown> | null | undefined;

    // 1. Validación de existencia del cuerpo de la petición
    if (!rawBody || typeof rawBody !== "object") {
      return {
        status: 400,
        body: {
          error: "BAD_REQUEST",
          mensaje: "El cuerpo de la petición HTTP no puede estar vacío y debe ser un objeto JSON válido."
        }
      };
    }

    // 2. Validación de conductorId
    const conductorId = rawBody.conductorId;
    if (!conductorId || typeof conductorId !== "string" || conductorId.trim().length === 0) {
      return {
        status: 400,
        body: {
          error: "CAMPO_REQUERIDO",
          campo: "conductorId",
          mensaje: "El identificador del conductor (conductorId) es obligatorio."
        }
      };
    }

    // 3. Validación de tipoEvento
    const tipoEvento = rawBody.tipoEvento;
    const tiposPermitidos = Object.values(TipoEvento) as string[];
    if (!tipoEvento || typeof tipoEvento !== "string") {
      return {
        status: 400,
        body: {
          error: "CAMPO_REQUERIDO",
          campo: "tipoEvento",
          mensaje: `El tipo de evento es obligatorio. Valores válidos: ${tiposPermitidos.join(", ")}.`
        }
      };
    }

    const tipoNormalizado = tipoEvento.toUpperCase();
    if (!tiposPermitidos.includes(tipoNormalizado)) {
      return {
        status: 400,
        body: {
          error: "VALOR_INVALIDO",
          campo: "tipoEvento",
          mensaje: `El tipo de evento '${tipoEvento}' no es válido. Valores soportados: ${tiposPermitidos.join(", ")}.`
        }
      };
    }

    // 4. Validación de silenciadoEnCabina
    const silenciadoEnCabina = rawBody.silenciadoEnCabina;
    if (typeof silenciadoEnCabina !== "boolean") {
      return {
        status: 400,
        body: {
          error: "CAMPO_REQUERIDO",
          campo: "silenciadoEnCabina",
          mensaje: "El indicador de silenciado en cabina (silenciadoEnCabina) debe ser un valor booleano (true/false)."
        }
      };
    }

    // 5. Validación opcional de coordenadas GPS
    let ubicacion: AlertaEntradaDto["ubicacion"];
    if (rawBody.ubicacion !== undefined && rawBody.ubicacion !== null) {
      const u = rawBody.ubicacion as Record<string, unknown>;
      const lat = Number(u.latitud);
      const lng = Number(u.longitud);

      if (isNaN(lat) || lat < -90 || lat > 90) {
        return {
          status: 400,
          body: {
            error: "COORDENADA_INVALIDA",
            campo: "ubicacion.latitud",
            mensaje: `Latitud inválida (${u.latitud}). Debe ser un número entre -90 y 90.`
          }
        };
      }

      if (isNaN(lng) || lng < -180 || lng > 180) {
        return {
          status: 400,
          body: {
            error: "COORDENADA_INVALIDA",
            campo: "ubicacion.longitud",
            mensaje: `Longitud inválida (${u.longitud}). Debe ser un número entre -180 y 180.`
          }
        };
      }

      ubicacion = {
        latitud: lat,
        longitud: lng,
        altitud: u.altitud !== undefined ? Number(u.altitud) : undefined,
        precisionMetros: u.precisionMetros !== undefined ? Number(u.precisionMetros) : undefined
      };
    }

    // 6. Ensamblado del DTO limpio para el puerto de entrada
    const alertaDto: AlertaEntradaDto = {
      incidenteId: typeof rawBody.incidenteId === "string" ? rawBody.incidenteId : undefined,
      conductorId: conductorId.trim(),
      tipoEvento: tipoNormalizado as TipoEvento,
      timestamp: rawBody.timestamp ? new Date(String(rawBody.timestamp)) : new Date(),
      silenciadoEnCabina,
      ubicacion
    };

    // 7. Invocación del Caso de Uso
    try {
      const resultado = await this.casoDeUso.procesar(alertaDto);
      return {
        status: 200,
        body: {
          mensaje: "Alerta procesada exitosamente por tire_alert_service.",
          resultado
        }
      };
    } catch (error) {
      const errorMsg = error instanceof Error ? error.message : String(error);

      // Mapeo a códigos HTTP apropiados
      if (errorMsg.includes("no encontrado")) {
        return {
          status: 404,
          body: {
            error: "NO_ENCONTRADO",
            mensaje: errorMsg
          }
        };
      }

      return {
        status: 500,
        body: {
          error: "ERROR_INTERNO",
          mensaje: `Error al procesar la alerta: ${errorMsg}`
        }
      };
    }
  }
}
