import { ClienteFcmFalso } from "../adaptadores/salida/externos/ClienteFcmFalso";
import { ProveedorSmsFalso } from "../adaptadores/salida/externos/ProveedorSmsFalso";
import { NotificadorPushFcmSimulado } from "../adaptadores/salida/notificadores/NotificadorPushFcmSimulado";
import { NotificadorSmsSimulado } from "../adaptadores/salida/notificadores/NotificadorSmsSimulado";
import { NotificadorDashboardSimulado } from "../adaptadores/salida/notificadores/NotificadorDashboardSimulado";
import { NotificadorConReintento } from "../adaptadores/salida/notificadores/NotificadorConReintento";
import { RepositorioIncidentesEnMemoria } from "../adaptadores/salida/RepositorioIncidentesEnMemoria";
import { ConsultorPerfilSimulado } from "../adaptadores/salida/ConsultorPerfilSimulado";
import { FabricaPoliticas } from "../aplicacion/FabricaPoliticas";
import { ServicioEscalamiento } from "../aplicacion/ServicioEscalamiento";
import { ControladorAlertasRest } from "../adaptadores/entrada/ControladorAlertasRest";

/**
 * Composition Root / Inyector de Dependencias
 * Capa de Configuración de la Arquitectura Hexagonal.
 * 
 * Ensambla el grafo de objetos del sistema conectando adaptadores externos,
 * decoradores de resiliencia, repositorios, fábricas y el controlador REST
 * con el caso de uso central ServicioEscalamiento.
 */
export class ConfiguracionAlertService {
  // Clientes simulados de infraestructura (Adaptees)
  public readonly clienteFcm: ClienteFcmFalso;
  public readonly proveedorSms: ProveedorSmsFalso;

  // Adaptadores de salida base
  public readonly notificadorFcmBase: NotificadorPushFcmSimulado;
  public readonly notificadorSmsBase: NotificadorSmsSimulado;
  public readonly notificadorDashboardBase: NotificadorDashboardSimulado;

  // Adaptadores decorados con patrón Decorator (Resiliencia ante fallos)
  public readonly notificadorFcm: NotificadorConReintento;
  public readonly notificadorSms: NotificadorConReintento;
  public readonly notificadorDashboard: NotificadorConReintento;

  // Persistencia y perfiles
  public readonly repositorioIncidentes: RepositorioIncidentesEnMemoria;
  public readonly consultorPerfil: ConsultorPerfilSimulado;

  // Fábrica de estrategias (Factory Method)
  public readonly fabricaPoliticas: FabricaPoliticas;

  // Caso de uso central y controlador REST
  public readonly servicioEscalamiento: ServicioEscalamiento;
  public readonly controladorAlertasRest: ControladorAlertasRest;

  public constructor(opciones?: {
    maxReintentos?: number;
    retrasoBaseMs?: number;
    ventanaMinutosRecientes?: number;
  }) {
    const maxReintentos = opciones?.maxReintentos ?? 3;
    const retrasoBaseMs = opciones?.retrasoBaseMs ?? 15;
    const ventanaMinutos = opciones?.ventanaMinutosRecientes ?? 15;

    // 1. Instanciación de componentes simulados externos
    this.clienteFcm = new ClienteFcmFalso();
    this.proveedorSms = new ProveedorSmsFalso();

    // 2. Instanciación de adaptadores base (Patrón Adapter)
    this.notificadorFcmBase = new NotificadorPushFcmSimulado(this.clienteFcm);
    this.notificadorSmsBase = new NotificadorSmsSimulado(this.proveedorSms);
    this.notificadorDashboardBase = new NotificadorDashboardSimulado();

    // 3. Decoración con tolerancia a fallas y reintentos (Patrón Decorator)
    this.notificadorFcm = new NotificadorConReintento(
      this.notificadorFcmBase,
      maxReintentos,
      retrasoBaseMs
    );
    this.notificadorSms = new NotificadorConReintento(
      this.notificadorSmsBase,
      maxReintentos,
      retrasoBaseMs
    );
    this.notificadorDashboard = new NotificadorConReintento(
      this.notificadorDashboardBase,
      maxReintentos,
      retrasoBaseMs
    );

    // 4. Repositorio y consultor de perfiles
    this.repositorioIncidentes = new RepositorioIncidentesEnMemoria();
    this.consultorPerfil = new ConsultorPerfilSimulado();

    // 5. Fábrica de políticas (Patrón Factory Method)
    this.fabricaPoliticas = new FabricaPoliticas();

    // 6. Ensamblaje del caso de uso central
    this.servicioEscalamiento = new ServicioEscalamiento(
      this.consultorPerfil,
      this.repositorioIncidentes,
      [this.notificadorFcm, this.notificadorSms, this.notificadorDashboard],
      this.fabricaPoliticas,
      ventanaMinutos
    );

    // 7. Adaptador de entrada REST
    this.controladorAlertasRest = new ControladorAlertasRest(this.servicioEscalamiento);
  }

  /**
   * Método de conveniencia para reiniciar el estado de memoria entre demostraciones.
   */
  public async reiniciar(): Promise<void> {
    await this.repositorioIncidentes.limpiar();
    this.clienteFcm.limpiar();
    this.proveedorSms.limpiar();
    this.notificadorDashboardBase.limpiar();
    this.consultorPerfil.restaurar();
    FabricaPoliticas.limpiarRegistro();
  }
}
