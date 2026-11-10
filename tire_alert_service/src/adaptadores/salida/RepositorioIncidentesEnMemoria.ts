import { Incidente } from "../../dominio/incidente/Incidente";
import { RepositorioIncidentes } from "../../aplicacion/puertos/salida/RepositorioIncidentes";

/**
 * Adaptador de Salida: Repositorio de Incidentes en Memoria
 * 
 * Implementación de almacenamiento volátil para pruebas unitarias, de integración
 * y auditoría local sin requerir infraestructura externa de base de datos.
 */
export class RepositorioIncidentesEnMemoria implements RepositorioIncidentes {
  private readonly incidentes: Map<string, Incidente> = new Map();

  public async guardar(incidente: Incidente): Promise<void> {
    this.incidentes.set(incidente.id, incidente);
  }

  public async contarRecientes(conductorId: string, minutosVentana: number): Promise<number> {
    const ahora = Date.now();
    const ventanaMs = minutosVentana * 60 * 1000;
    const limiteInferior = ahora - ventanaMs;

    let contador = 0;
    for (const inc of this.incidentes.values()) {
      if (inc.conductorId === conductorId) {
        const fechaIncidente = inc.timestamp.getTime();
        if (fechaIncidente >= limiteInferior && fechaIncidente <= ahora) {
          contador++;
        }
      }
    }

    return contador;
  }

  public async buscarPorId(id: string): Promise<Incidente | null> {
    const inc = this.incidentes.get(id);
    return inc ?? null;
  }

  public async listarTodos(): Promise<Incidente[]> {
    return Array.from(this.incidentes.values());
  }

  public async limpiar(): Promise<void> {
    this.incidentes.clear();
  }
}
