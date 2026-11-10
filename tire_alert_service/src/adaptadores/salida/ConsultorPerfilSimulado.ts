import { ContextoConductor, TipoPerfilConductor } from "../../dominio/tipos";
import { ConsultorPerfil } from "../../aplicacion/puertos/salida/ConsultorPerfil";

/**
 * Adaptador de Salida: Consultor de Perfiles Simulado (Stub/Mock)
 * 
 * Precarga los perfiles canónicos del dominio Tire para pruebas y demostraciones:
 * - Particular Solo (COND-001)
 * - Particular con Copiloto (COND-002)
 * - Conductor de Flota de Carga Crítica (COND-003)
 */
export class ConsultorPerfilSimulado implements ConsultorPerfil {
  private readonly perfiles: Map<string, ContextoConductor> = new Map();

  public constructor() {
    this.cargarPerfilesPorDefecto();
  }

  private cargarPerfilesPorDefecto(): void {
    // 1. Conductor Particular Solo (Requiere SMS a contactos de emergencia)
    this.perfiles.set("COND-001", {
      conductorId: "COND-001",
      nombre: "Carlos Gómez",
      tipoPerfil: TipoPerfilConductor.PARTICULAR_SOLO,
      telefono: "+573001112233",
      contactosEmergencia: [
        {
          nombre: "Laura Gómez (Esposa)",
          telefono: "+573104445566",
          relacion: "Cónyuge"
        },
        {
          nombre: "Pedro Gómez (Hermano)",
          telefono: "+573207778899",
          relacion: "Hermano"
        }
      ]
    });

    // 2. Conductor Particular con Copiloto (Requiere Push FCM al tripulante)
    this.perfiles.set("COND-002", {
      conductorId: "COND-002",
      nombre: "Mariana Rivas",
      tipoPerfil: TipoPerfilConductor.PARTICULAR_CON_COPILOTO,
      telefono: "+573152223344",
      copiloto: {
        nombre: "Andrés Silva",
        tokenFcm: "fcm_token_copiloto_andres_xyz987",
        telefono: "+573168889900"
      }
    });

    // 3. Conductor de Flota de Transporte de Carga Crítica (Requiere Alerta en Dashboard B2B)
    this.perfiles.set("COND-003", {
      conductorId: "COND-003",
      nombre: "Javier Mendoza",
      tipoPerfil: TipoPerfilConductor.FLOTA,
      telefono: "+573129990011",
      flota: {
        empresaId: "TRANS_CARGA_NACIONAL_SAS",
        despachadorId: "DESPACHADOR_BOGOTA_ZONA_NORTE",
        canalDashboard: "dashboard-flota-transcarga",
        placaVehiculo: "WKA-789"
      }
    });
  }

  public async obtenerContexto(conductorId: string): Promise<ContextoConductor | null> {
    const perfil = this.perfiles.get(conductorId);
    return perfil ? JSON.parse(JSON.stringify(perfil)) : null;
  }

  /**
   * Permite inyectar perfiles dinámicos en tiempo de ejecución (útil para pruebas OCP).
   */
  public registrarPerfil(contexto: ContextoConductor): void {
    this.perfiles.set(contexto.conductorId, contexto);
  }

  public limpiar(): void {
    this.perfiles.clear();
  }

  public restaurar(): void {
    this.limpiar();
    this.cargarPerfilesPorDefecto();
  }
}
