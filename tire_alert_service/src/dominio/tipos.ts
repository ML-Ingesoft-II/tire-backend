/**
 * Definiciones de tipos, enums y contratos fundamentales del dominio.
 * Capa de Dominio - Cero dependencias de frameworks externos.
 */

export enum TipoEvento {
  MICROSUEÑO = "MICROSUEÑO",
  MICROSUENO = "MICROSUENO",
  BOSTEZO_REPETITIVO = "BOSTEZO_REPETITIVO",
  DISTRACTOR_PROLONGADO = "DISTRACTOR_PROLONGADO",
  PARPADEO_LENTO = "PARPADEO_LENTO",
  POSTURA_INADECUADA = "POSTURA_INADECUADA"
}

export enum NivelRiesgo {
  NORMAL = "NORMAL",
  PRECAUCION = "PRECAUCION",
  MEDIO = "MEDIO",
  ALTO = "ALTO",
  CRITICO = "CRITICO"
}

export enum CanalNotificacion {
  SMS = "SMS",
  PUSH_FCM = "PUSH_FCM",
  DASHBOARD_WEB = "DASHBOARD_WEB"
}

export enum TipoPerfilConductor {
  PARTICULAR_SOLO = "PARTICULAR_SOLO",
  PARTICULAR_CON_COPILOTO = "PARTICULAR_CON_COPILOTO",
  FLOTA = "FLOTA"
}

export interface UbicacionGps {
  latitud: number;
  longitud: number;
  altitud?: number;
  precisionMetros?: number;
}

export interface ContactoEmergencia {
  nombre: string;
  telefono: string;
  relacion?: string;
}

export interface InfoCopiloto {
  nombre: string;
  tokenFcm: string;
  telefono?: string;
}

export interface InfoFlota {
  empresaId: string;
  despachadorId: string;
  canalDashboard: string;
  placaVehiculo?: string;
}

export interface ContextoConductor {
  conductorId: string;
  nombre: string;
  tipoPerfil: TipoPerfilConductor | string;
  telefono?: string;
  copiloto?: InfoCopiloto;
  contactosEmergencia?: ContactoEmergencia[];
  flota?: InfoFlota;
  metadatos?: Record<string, unknown>;
}

export interface AccionNotificacion {
  canal: CanalNotificacion;
  destinatario: string;
  mensaje: string;
  metadatos?: Record<string, unknown>;
}

export interface ResultadoEnvio {
  exitoso: boolean;
  canal: CanalNotificacion;
  destinatario: string;
  timestamp: Date;
  error?: string;
  reintentos?: number;
}

export interface AlertaEntradaDto {
  incidenteId?: string;
  conductorId: string;
  tipoEvento: TipoEvento | string;
  timestamp?: string | Date;
  silenciadoEnCabina: boolean;
  ubicacion?: {
    latitud: number;
    longitud: number;
    altitud?: number;
    precisionMetros?: number;
  };
}

export interface ResultadoProcesamiento {
  incidenteId: string;
  conductorId: string;
  escalado: boolean;
  estadoFinal: string;
  nivelRiesgoFinal: NivelRiesgo;
  motivoEscalamiento?: string;
  notificacionesDespachadas: ResultadoEnvio[];
  persistenciaExitosa: boolean;
}

export interface PeticionHttp {
  headers?: Record<string, string>;
  body: unknown;
  params?: Record<string, string>;
  query?: Record<string, string>;
}

export interface RespuestaHttp {
  status: number;
  body: unknown;
}
