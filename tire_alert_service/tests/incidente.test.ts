import { IncidenteBuilder } from "../src/dominio/incidente/IncidenteBuilder";
import { EstadoDetectado } from "../src/dominio/incidente/estados/EstadoDetectado";
import { EstadoEscalado } from "../src/dominio/incidente/estados/EstadoEscalado";
import { EstadoAtendido } from "../src/dominio/incidente/estados/EstadoAtendido";
import { NivelRiesgo, TipoEvento } from "../src/dominio/tipos";

describe("Agregado Incidente: Patrón Builder y Patrón State", () => {
  // ---------------------------------------------------------------------------
  // 1. PATRÓN BUILDER: IncidenteBuilder
  // ---------------------------------------------------------------------------
  describe("Patrón Builder: IncidenteBuilder", () => {
    it("debe construir un Incidente válido con todos sus atributos requeridos y opcionales", () => {
      const fecha = new Date("2026-11-09T10:00:00Z");
      const incidente = new IncidenteBuilder()
        .conId("INC-999")
        .conConductorId("COND-123")
        .conTipoEvento(TipoEvento.MICROSUEÑO)
        .conTimestamp(fecha)
        .conSilenciadoEnCabina(false)
        .conUbicacion(4.7110, -74.0721, 2600, 5)
        .construir();

      expect(incidente.id).toBe("INC-999");
      expect(incidente.conductorId).toBe("COND-123");
      expect(incidente.tipoEvento).toBe(TipoEvento.MICROSUEÑO);
      expect(incidente.timestamp.getTime()).toBe(fecha.getTime());
      expect(incidente.silenciadoEnCabina).toBe(false);
      expect(incidente.ubicacion).toEqual({
        latitud: 4.7110,
        longitud: -74.0721,
        altitud: 2600,
        precisionMetros: 5
      });
      expect(incidente.obtenerNombreEstado()).toBe("DETECTADO");
      expect(incidente.obtenerNivelRiesgo()).toBe(NivelRiesgo.PRECAUCION);
    });

    it("debe autogenerar un ID y fecha actual si no se suministran explícitamente", () => {
      const antes = Date.now();
      const incidente = new IncidenteBuilder()
        .conConductorId("COND-001")
        .conTipoEvento(TipoEvento.PARPADEO_LENTO)
        .conSilenciadoEnCabina(true)
        .construir();

      expect(incidente.id).toBeDefined();
      expect(incidente.id).toContain("INC-");
      expect(incidente.timestamp.getTime()).toBeGreaterThanOrEqual(antes);
      expect(incidente.ubicacion).toBeUndefined();
    });

    it("debe lanzar un error si falta conductorId", () => {
      expect(() => {
        new IncidenteBuilder()
          .conTipoEvento(TipoEvento.MICROSUEÑO)
          .conSilenciadoEnCabina(false)
          .construir();
      }).toThrow("No se puede construir un Incidente sin conductorId");
    });

    it("debe lanzar un error si falta tipoEvento o es inválido", () => {
      expect(() => {
        new IncidenteBuilder().conTipoEvento("EVENTO_INVENTADO");
      }).toThrow("Tipo de evento 'EVENTO_INVENTADO' no reconocido");
    });

    it("debe lanzar un error si falta silenciadoEnCabina", () => {
      expect(() => {
        new IncidenteBuilder()
          .conConductorId("COND-001")
          .conTipoEvento(TipoEvento.BOSTEZO_REPETITIVO)
          .construir();
      }).toThrow("No se puede construir un Incidente sin especificar silenciadoEnCabina");
    });

    it("debe validar estrictamente los rangos de coordenadas GPS", () => {
      const builder = new IncidenteBuilder();

      // Latitud fuera de [-90, 90]
      expect(() => builder.conUbicacion(95, -74)).toThrow("Latitud inválida (95)");
      expect(() => builder.conUbicacion(-95, -74)).toThrow("Latitud inválida (-95)");

      // Longitud fuera de [-180, 180]
      expect(() => builder.conUbicacion(4, 185)).toThrow("Longitud inválida (185)");
      expect(() => builder.conUbicacion(4, -185)).toThrow("Longitud inválida (-185)");
    });
  });

  // ---------------------------------------------------------------------------
  // 2. PATRÓN STATE: Ciclo de vida y transiciones del Incidente
  // ---------------------------------------------------------------------------
  describe("Patrón State: Estados y Transiciones de Incidente", () => {
    let incidente: ReturnType<typeof IncidenteBuilder.prototype.construir>;

    beforeEach(() => {
      incidente = new IncidenteBuilder()
        .conId("INC-STATE-01")
        .conConductorId("COND-01")
        .conTipoEvento(TipoEvento.MICROSUEÑO)
        .conSilenciadoEnCabina(false)
        .construir();
    });

    it("debe iniciar en EstadoDetectado con riesgo PRECAUCION", () => {
      expect(incidente.estado).toBeInstanceOf(EstadoDetectado);
      expect(incidente.obtenerNombreEstado()).toBe("DETECTADO");
      expect(incidente.obtenerNivelRiesgo()).toBe(NivelRiesgo.PRECAUCION);
      expect(incidente.estaEscalado()).toBe(false);
      expect(incidente.estaAtendido()).toBe(false);
    });

    it("debe transicionar de EstadoDetectado a EstadoEscalado al llamar escalar()", () => {
      incidente.escalar("El conductor no respondió a la alarma sonora de cabina.");

      expect(incidente.estado).toBeInstanceOf(EstadoEscalado);
      expect(incidente.obtenerNombreEstado()).toBe("ESCALADO");
      expect(incidente.obtenerNivelRiesgo()).toBe(NivelRiesgo.CRITICO);
      expect(incidente.estaEscalado()).toBe(true);
      expect(incidente.estaAtendido()).toBe(false);

      const historial = incidente.obtenerHistorial();
      expect(historial).toHaveLength(2);
      expect(historial[1].estado).toBe("ESCALADO");
      expect(historial[1].motivo).toContain("alarma sonora de cabina");
    });

    it("debe transicionar de EstadoEscalado a EstadoAtendido al llamar atender()", () => {
      incidente.escalar("Escalamiento a despachador");
      expect(incidente.estaEscalado()).toBe(true);

      incidente.atender("Despachador confirmó por radio que el vehículo se detuvo en bahía segura.");

      expect(incidente.estado).toBeInstanceOf(EstadoAtendido);
      expect(incidente.obtenerNombreEstado()).toBe("ATENDIDO");
      expect(incidente.obtenerNivelRiesgo()).toBe(NivelRiesgo.NORMAL);
      expect(incidente.estaAtendido()).toBe(true);

      const historial = incidente.obtenerHistorial();
      expect(historial).toHaveLength(3);
      expect(historial[2].estado).toBe("ATENDIDO");
      expect(historial[2].motivo).toContain("bahía segura");
    });

    it("debe permitir atender directamente desde EstadoDetectado si el conductor se detuvo en cabina", () => {
      incidente.atender("Conductor se estacionó voluntariamente tras aviso preventivo.");

      expect(incidente.estado).toBeInstanceOf(EstadoAtendido);
      expect(incidente.obtenerNombreEstado()).toBe("ATENDIDO");
      expect(incidente.obtenerNivelRiesgo()).toBe(NivelRiesgo.NORMAL);
    });

    it("debe lanzar un error si se intenta escalar un incidente que ya fue ATENDIDO (Estado Terminal)", () => {
      incidente.atender("Resuelto");
      expect(incidente.estaAtendido()).toBe(true);

      expect(() => {
        incidente.escalar("Intento de re-escalar");
      }).toThrow("No se puede escalar un incidente que ya fue resuelto");
    });

    it("debe ser idempotente si se invoca escalar() cuando ya está en EstadoEscalado", () => {
      incidente.escalar("Primer escalamiento");
      expect(incidente.estaEscalado()).toBe(true);

      // Segundo escalamiento no debe fallar ni duplicar estados indebidamente
      expect(() => incidente.escalar("Segundo escalamiento")).not.toThrow();
      expect(incidente.estaEscalado()).toBe(true);
    });
  });
});
