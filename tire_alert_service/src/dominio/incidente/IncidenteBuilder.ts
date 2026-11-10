import { TipoEvento, UbicacionGps } from "../tipos";
import { Incidente } from "./Incidente";
import { EstadoIncidente } from "./estados/EstadoIncidente";
import { EstadoDetectado } from "./estados/EstadoDetectado";

/**
 * Patrón de Diseño GoF: Builder (Creacional)
 * 
 * Permite la construcción paso a paso y validada del agregado Incidente.
 * Resuelve el problema del constructor telescópico ante parámetros opcionales
 * (como coordenadas GPS que fallan dentro de túneles o pasos montañosos) y
 * asegura invariantes de negocio antes de instanciar el objeto.
 */
export class IncidenteBuilder {
  private _id?: string;
  private _conductorId?: string;
  private _tipoEvento?: TipoEvento;
  private _timestamp: Date = new Date();
  private _silenciadoEnCabina?: boolean;
  private _ubicacion?: UbicacionGps;
  private _estadoInicial?: EstadoIncidente;

  public conId(id: string): this {
    if (!id || id.trim().length === 0) {
      throw new Error("El ID del incidente no puede estar vacío.");
    }
    this._id = id.trim();
    return this;
  }

  public conConductorId(conductorId: string): this {
    if (!conductorId || conductorId.trim().length === 0) {
      throw new Error("El ID del conductor es obligatorio para crear el incidente.");
    }
    this._conductorId = conductorId.trim();
    return this;
  }

  public conTipoEvento(tipo: TipoEvento | string): this {
    if (!tipo) {
      throw new Error("El tipo de evento de fatiga es obligatorio.");
    }
    const valoresValidos = Object.values(TipoEvento) as string[];
    const normalizado = String(tipo).toUpperCase();
    if (!valoresValidos.includes(normalizado)) {
      throw new Error(
        `Tipo de evento '${tipo}' no reconocido. Tipos válidos: ${valoresValidos.join(", ")}.`
      );
    }
    this._tipoEvento = normalizado as TipoEvento;
    return this;
  }

  public conTimestamp(timestamp: Date | string | number): this {
    const fecha = new Date(timestamp);
    if (isNaN(fecha.getTime())) {
      throw new Error("Timestamp de incidente inválido.");
    }
    this._timestamp = fecha;
    return this;
  }

  public conSilenciadoEnCabina(silenciado: boolean): this {
    if (typeof silenciado !== "boolean") {
      throw new Error("El indicador de silenciado en cabina debe ser un valor booleano.");
    }
    this._silenciadoEnCabina = silenciado;
    return this;
  }

  public conUbicacion(
    latitud: number,
    longitud: number,
    altitud?: number,
    precisionMetros?: number
  ): this {
    if (typeof latitud !== "number" || latitud < -90 || latitud > 90) {
      throw new Error(`Latitud inválida (${latitud}). Debe estar en el rango [-90, 90].`);
    }
    if (typeof longitud !== "number" || longitud < -180 || longitud > 180) {
      throw new Error(`Longitud inválida (${longitud}). Debe estar en el rango [-180, 180].`);
    }
    this._ubicacion = {
      latitud,
      longitud,
      altitud,
      precisionMetros
    };
    return this;
  }

  public sinUbicacion(): this {
    this._ubicacion = undefined;
    return this;
  }

  public conEstadoInicial(estado: EstadoIncidente): this {
    this._estadoInicial = estado;
    return this;
  }

  /**
   * Construye el agregado Incidente validando exhaustivamente sus invariantes.
   */
  public construir(): Incidente {
    if (!this._conductorId) {
      throw new Error("No se puede construir un Incidente sin conductorId.");
    }
    if (!this._tipoEvento) {
      throw new Error("No se puede construir un Incidente sin tipoEvento.");
    }
    if (this._silenciadoEnCabina === undefined) {
      throw new Error("No se puede construir un Incidente sin especificar silenciadoEnCabina.");
    }

    const idFinal = this._id ?? `INC-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`;
    const estadoFinal = this._estadoInicial ?? new EstadoDetectado();

    return new Incidente(
      idFinal,
      this._conductorId,
      this._tipoEvento,
      this._timestamp,
      this._silenciadoEnCabina,
      this._ubicacion,
      estadoFinal
    );
  }
}
