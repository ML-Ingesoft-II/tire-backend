import { NivelRiesgo, TipoEvento, UbicacionGps } from "../tipos";
import { EstadoIncidente } from "./estados/EstadoIncidente";
import { EstadoDetectado } from "./estados/EstadoDetectado";

export interface TransicionEstado {
  estado: string;
  fecha: Date;
  motivo?: string;
}

/**
 * Entidad y Agregado Raíz del Dominio: Incidente
 * Actúa como el 'Context' en el Patrón de Diseño GoF: State.
 * 
 * Delega el comportamiento dependiente del ciclo de vida y la evaluación del nivel de riesgo
 * a su objeto de estado actual.
 */
export class Incidente {
  private readonly _id: string;
  private readonly _conductorId: string;
  private readonly _tipoEvento: TipoEvento;
  private readonly _timestamp: Date;
  private readonly _silenciadoEnCabina: boolean;
  private readonly _ubicacion?: UbicacionGps;

  private _estado: EstadoIncidente;
  private readonly _historial: TransicionEstado[] = [];

  constructor(
    id: string,
    conductorId: string,
    tipoEvento: TipoEvento,
    timestamp: Date,
    silenciadoEnCabina: boolean,
    ubicacion?: UbicacionGps,
    estadoInicial?: EstadoIncidente
  ) {
    this._id = id;
    this._conductorId = conductorId;
    this._tipoEvento = tipoEvento;
    this._timestamp = timestamp;
    this._silenciadoEnCabina = silenciadoEnCabina;
    this._ubicacion = ubicacion;

    this._estado = estadoInicial ?? new EstadoDetectado();
    this._historial.push({
      estado: this._estado.nombre,
      fecha: new Date(timestamp),
      motivo: "Creación inicial del incidente tras detección en cabina."
    });
  }

  public get id(): string {
    return this._id;
  }

  public get conductorId(): string {
    return this._conductorId;
  }

  public get tipoEvento(): TipoEvento {
    return this._tipoEvento;
  }

  public get timestamp(): Date {
    return new Date(this._timestamp);
  }

  public get silenciadoEnCabina(): boolean {
    return this._silenciadoEnCabina;
  }

  public get ubicacion(): UbicacionGps | undefined {
    return this._ubicacion ? { ...this._ubicacion } : undefined;
  }

  public get estado(): EstadoIncidente {
    return this._estado;
  }

  /**
   * Cambia el estado actual y registra la transición en la bitácora interna.
   * Invocado por los estados concretos al ejecutar transiciones válidas.
   */
  public cambiarEstado(nuevoEstado: EstadoIncidente, motivo?: string): void {
    this._estado = nuevoEstado;
    this._historial.push({
      estado: nuevoEstado.nombre,
      fecha: new Date(),
      motivo
    });
  }

  /**
   * Delega la transición al estado actual (Patrón State).
   */
  public escalar(motivo?: string): void {
    this._estado.escalar(this, motivo);
  }

  /**
   * Delega la transición al estado actual (Patrón State).
   */
  public atender(motivo?: string): void {
    this._estado.atender(this, motivo);
  }

  /**
   * Consulta polimórfica del nivel de riesgo delegada en el estado.
   */
  public obtenerNivelRiesgo(): NivelRiesgo {
    return this._estado.obtenerNivelRiesgo();
  }

  public obtenerNombreEstado(): string {
    return this._estado.nombre;
  }

  public estaEscalado(): boolean {
    return this._estado.nombre === "ESCALADO";
  }

  public estaAtendido(): boolean {
    return this._estado.nombre === "ATENDIDO";
  }

  public obtenerHistorial(): ReadonlyArray<TransicionEstado> {
    return [...this._historial];
  }
}
