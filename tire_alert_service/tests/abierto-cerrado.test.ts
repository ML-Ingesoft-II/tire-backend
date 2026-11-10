import { PoliticaEscalamiento } from "../src/dominio/politicas/PoliticaEscalamiento";
import { Incidente } from "../src/dominio/incidente/Incidente";
import { FabricaPoliticas } from "../src/aplicacion/FabricaPoliticas";
import { ServicioEscalamiento } from "../src/aplicacion/ServicioEscalamiento";
import { ConsultorPerfilSimulado } from "../src/adaptadores/salida/ConsultorPerfilSimulado";
import { RepositorioIncidentesEnMemoria } from "../src/adaptadores/salida/RepositorioIncidentesEnMemoria";
import { NotificadorDashboardSimulado } from "../src/adaptadores/salida/notificadores/NotificadorDashboardSimulado";
import { NotificadorSmsSimulado } from "../src/adaptadores/salida/notificadores/NotificadorSmsSimulado";
import { ProveedorSmsFalso } from "../src/adaptadores/salida/externos/ProveedorSmsFalso";
import {
  AccionNotificacion,
  CanalNotificacion,
  ContextoConductor,
  TipoEvento
} from "../src/dominio/tipos";

/**
 * =========================================================================================
 * FASE 7: DEMOSTRACIÓN DEL PRINCIPIO ABIERTO / CERRADO (OCP - OPEN/CLOSED PRINCIPLE)
 * =========================================================================================
 * 
 * Requerimiento: Demostrar cómo incorporar un nuevo perfil de negocio (TRANSPORTE_PUBLICO)
 * con una política de escalamiento especializada (PoliticaTransportePublico) y extendiendo
 * la fábrica de políticas (FabricaPoliticas) SIN MODIFICAR NI UNA SOLA LÍNEA DE CÓDIGO
 * EXISTENTE EN EL DIRECTORIO src/.
 */

/**
 * NUEVA POLÍTICA DE NEGOCIO: Creada 100% fuera de src/
 * Modela el escalamiento para buses de transporte público masivo e intermunicipal.
 * 
 * Regla de negocio de Transporte Público:
 * Ante un microsueño de un conductor de bus con pasajeros, se debe:
 * 1. Alertar al Centro de Control de Tránsito / Terminal (DASHBOARD_WEB).
 * 2. Alertar por SMS al Director de Operaciones de la empresa transportadora para activar relevo inmediato.
 */
class PoliticaTransportePublico implements PoliticaEscalamiento {
  public determinarAcciones(
    incidente: Incidente,
    contexto: ContextoConductor
  ): AccionNotificacion[] {
    const acciones: AccionNotificacion[] = [];

    const meta = (contexto.metadatos ?? {}) as {
      ruta?: string;
      capacidadPasajeros?: number;
      telefonoDirectorOperaciones?: string;
    };

    const coordsStr = incidente.ubicacion
      ? `Lat: ${incidente.ubicacion.latitud}, Lng: ${incidente.ubicacion.longitud}`
      : "GPS: Sin cobertura satelital";

    // 1. Notificación crítica al Centro de Control de Transporte Público
    acciones.push({
      canal: CanalNotificacion.DASHBOARD_WEB,
      destinatario: "centro-control-transporte-publico",
      mensaje: `[EMERGENCIA TRANSPORTE PÚBLICO] Conductor ${contexto.nombre} (ID: ${contexto.conductorId}) en Ruta ${meta.ruta ?? "Intermunicipal"} presentó evento de fatiga (${incidente.tipoEvento}). ${meta.capacidadPasajeros ?? 40} pasajeros a bordo. Ubicación: ${coordsStr}. Ordenar detención preventiva en terminal o CAI más cercano.`,
      metadatos: {
        tipoTransporte: "TRANSPORTE_PUBLICO",
        ruta: meta.ruta,
        pasajerosEnRiesgo: meta.capacidadPasajeros,
        incidenteId: incidente.id,
        nivelRiesgo: incidente.obtenerNivelRiesgo()
      }
    });

    // 2. Notificación SMS al Director de Operaciones
    if (meta.telefonoDirectorOperaciones) {
      acciones.push({
        canal: CanalNotificacion.SMS,
        destinatario: meta.telefonoDirectorOperaciones,
        mensaje: `[ALERTA DIRECCIÓN OPERACIONES] Conductor ${contexto.nombre} en Ruta ${meta.ruta} en estado crítico de somnolencia. Despachar tripulación de relevo inmediato.`,
        metadatos: {
          incidenteId: incidente.id
        }
      });
    }

    return acciones;
  }
}

/**
 * EXTENSIÓN DE LA FÁBRICA VÍA SUBCLASE (Factory Method clásico GoF)
 * Demuestra la extensión por herencia sin modificar la clase base en src/.
 */
class FabricaPoliticasTransportePublico extends FabricaPoliticas {
  protected override crearPoliticaConcreta(
    tipo: string,
    contexto: ContextoConductor
  ): PoliticaEscalamiento {
    if (tipo === "TRANSPORTE_PUBLICO") {
      return new PoliticaTransportePublico();
    }
    // Delega al comportamiento base para los perfiles preexistentes
    return super.crearPoliticaConcreta(tipo, contexto);
  }
}

describe("Demostración OCP (Abierto/Cerrado) - Fase 7: Perfil TRANSPORTE_PUBLICO", () => {
  let consultorPerfil: ConsultorPerfilSimulado;
  let repositorio: RepositorioIncidentesEnMemoria;
  let dashboard: NotificadorDashboardSimulado;
  let proveedorSms: ProveedorSmsFalso;
  let notificadorSms: NotificadorSmsSimulado;

  beforeEach(async () => {
    consultorPerfil = new ConsultorPerfilSimulado();
    repositorio = new RepositorioIncidentesEnMemoria();
    dashboard = new NotificadorDashboardSimulado();
    proveedorSms = new ProveedorSmsFalso();
    notificadorSms = new NotificadorSmsSimulado(proveedorSms);
    FabricaPoliticas.limpiarRegistro();
  });

  afterEach(() => {
    FabricaPoliticas.limpiarRegistro();
  });

  // ---------------------------------------------------------------------------
  // ENFOQUE A: Extensión mediante Subclase de Factory Method
  // ---------------------------------------------------------------------------
  it("debe procesar TRANSPORTE_PUBLICO extendiendo FabricaPoliticas mediante herencia sin tocar src/", async () => {
    // 1. Instanciamos la fábrica extendida creada exclusivamente en este archivo de pruebas
    const fabricaExtendida = new FabricaPoliticasTransportePublico();

    // 2. Instanciamos el servicio de aplicación inyectando la nueva fábrica
    const servicio = new ServicioEscalamiento(
      consultorPerfil,
      repositorio,
      [dashboard, notificadorSms],
      fabricaExtendida
    );

    // 3. Registramos un conductor de transporte público en el consultor de perfiles
    consultorPerfil.registrarPerfil({
      conductorId: "COND-BUS-777",
      nombre: "Hernando Duque",
      tipoPerfil: "TRANSPORTE_PUBLICO",
      metadatos: {
        ruta: "Bogotá - Tunja (Flota Boyacá)",
        capacidadPasajeros: 45,
        telefonoDirectorOperaciones: "+573110009988"
      }
    });

    // 4. Procesamos una alerta de microsueño no silenciada
    const resultado = await servicio.procesar({
      conductorId: "COND-BUS-777",
      tipoEvento: TipoEvento.MICROSUEÑO,
      silenciadoEnCabina: false,
      ubicacion: {
        latitud: 5.5352,
        longitud: -73.3677
      }
    });

    // 5. Validaciones rigurosas
    expect(resultado.escalado).toBe(true);
    expect(resultado.estadoFinal).toBe("ESCALADO");
    expect(resultado.nivelRiesgoFinal).toBe("CRITICO");
    expect(resultado.notificacionesDespachadas).toHaveLength(2);

    // Verificamos que se despachó al Dashboard del Centro de Control
    expect(resultado.notificacionesDespachadas[0].canal).toBe(CanalNotificacion.DASHBOARD_WEB);
    expect(resultado.notificacionesDespachadas[0].destinatario).toBe("centro-control-transporte-publico");

    const eventosDashboard = dashboard.obtenerHistorial();
    expect(eventosDashboard).toHaveLength(1);
    expect(eventosDashboard[0].mensaje).toContain("45 pasajeros a bordo");
    expect(eventosDashboard[0].mensaje).toContain("Hernando Duque");
    expect(eventosDashboard[0].mensaje).toContain("Flota Boyacá");

    // Verificamos que se despachó SMS al Director de Operaciones
    expect(resultado.notificacionesDespachadas[1].canal).toBe(CanalNotificacion.SMS);
    expect(resultado.notificacionesDespachadas[1].destinatario).toBe("+573110009988");

    const eventosSms = proveedorSms.obtenerHistorial();
    expect(eventosSms).toHaveLength(1);
    expect(eventosSms[0].contenido).toContain("tripulación de relevo inmediato");
  });

  // ---------------------------------------------------------------------------
  // ENFOQUE B: Extensión mediante Registro Dinámico (Open/Closed Registry)
  // ---------------------------------------------------------------------------
  it("debe procesar TRANSPORTE_PUBLICO mediante el registro dinámico de FabricaPoliticas sin tocar src/", async () => {
    // 1. Registramos la nueva política directamente en la clase base usando el método estático
    FabricaPoliticas.registrarPolitica("TRANSPORTE_PUBLICO", () => new PoliticaTransportePublico());

    // 2. Usamos la fábrica estándar existente en src/
    const fabricaEstandar = new FabricaPoliticas();

    const servicio = new ServicioEscalamiento(
      consultorPerfil,
      repositorio,
      [dashboard, notificadorSms],
      fabricaEstandar
    );

    consultorPerfil.registrarPerfil({
      conductorId: "COND-METROBUS-55",
      nombre: "Clara Inés Morales",
      tipoPerfil: "TRANSPORTE_PUBLICO",
      metadatos: {
        ruta: "Troncal Caracas - Transmilenio",
        capacidadPasajeros: 160,
        telefonoDirectorOperaciones: "+573155556677"
      }
    });

    const resultado = await servicio.procesar({
      conductorId: "COND-METROBUS-55",
      tipoEvento: TipoEvento.PARPADEO_LENTO,
      silenciadoEnCabina: false
    });

    expect(resultado.escalado).toBe(true);
    expect(resultado.notificacionesDespachadas).toHaveLength(2);

    const eventosDashboard = dashboard.obtenerHistorial();
    expect(eventosDashboard[0].mensaje).toContain("160 pasajeros a bordo");
    expect(eventosDashboard[0].mensaje).toContain("Troncal Caracas");
  });
});
