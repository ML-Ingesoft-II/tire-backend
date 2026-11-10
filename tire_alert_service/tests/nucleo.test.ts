import { ReglasEscalamiento } from "../src/dominio/ReglasEscalamiento";
import { FabricaPoliticas } from "../src/aplicacion/FabricaPoliticas";
import { PoliticaParticularSolo } from "../src/dominio/politicas/PoliticaParticularSolo";
import { PoliticaParticularConCopiloto } from "../src/dominio/politicas/PoliticaParticularConCopiloto";
import { PoliticaFlota } from "../src/dominio/politicas/PoliticaFlota";
import { IncidenteBuilder } from "../src/dominio/incidente/IncidenteBuilder";
import {
  CanalNotificacion,
  ContextoConductor,
  TipoEvento,
  TipoPerfilConductor
} from "../src/dominio/tipos";

describe("Núcleo del Dominio y Aplicación: Strategy, Factory Method y Reglas de Negocio", () => {
  // ---------------------------------------------------------------------------
  // 1. REGLAS DE ESCALAMIENTO (Lógica de Dominio Pura)
  // ---------------------------------------------------------------------------
  describe("ReglasEscalamiento (Lógica pura: !silenciado || repetidas >= umbral)", () => {
    it("debe escalar si la alerta NO fue silenciada en cabina, sin importar eventos previos", () => {
      // Caso 1: 0 eventos previos, no silenciada -> DEBE ESCALAR
      expect(ReglasEscalamiento.debeEscalar(false, 0)).toBe(true);

      // Caso 2: 5 eventos previos, no silenciada -> DEBE ESCALAR
      expect(ReglasEscalamiento.debeEscalar(false, 5)).toBe(true);

      const motivo = ReglasEscalamiento.obtenerMotivo(false, 0);
      expect(motivo).toContain("no fue atendida/silenciada");
    });

    it("NO debe escalar si fue silenciada y los eventos recientes son menores al umbral (3)", () => {
      expect(ReglasEscalamiento.debeEscalar(true, 0)).toBe(false);
      expect(ReglasEscalamiento.debeEscalar(true, 1)).toBe(false);
      expect(ReglasEscalamiento.debeEscalar(true, 2)).toBe(false);

      const motivo = ReglasEscalamiento.obtenerMotivo(true, 1);
      expect(motivo).toContain("No requiere escalamiento");
    });

    it("debe escalar si fue silenciada pero acumula eventos >= umbral por defecto (3)", () => {
      expect(ReglasEscalamiento.debeEscalar(true, 3)).toBe(true);
      expect(ReglasEscalamiento.debeEscalar(true, 7)).toBe(true);

      const motivo = ReglasEscalamiento.obtenerMotivo(true, 3);
      expect(motivo).toContain("umbral de fatiga acumulada");
    });

    it("debe respetar un umbral personalizado si se suministra", () => {
      const umbralPersonalizado = 5;
      expect(ReglasEscalamiento.debeEscalar(true, 4, umbralPersonalizado)).toBe(false);
      expect(ReglasEscalamiento.debeEscalar(true, 5, umbralPersonalizado)).toBe(true);
    });
  });

  // ---------------------------------------------------------------------------
  // 2. PATRÓN STRATEGY: Políticas de Escalamiento
  // ---------------------------------------------------------------------------
  describe("Patrón Strategy: Políticas de Escalamiento Concretas", () => {
    const builderBase = () =>
      new IncidenteBuilder()
        .conId("INC-TEST-001")
        .conConductorId("COND-TEST")
        .conTipoEvento(TipoEvento.MICROSUEÑO)
        .conSilenciadoEnCabina(false);

    it("PoliticaParticularSolo debe despachar SMS a todos los contactos de emergencia con Google Maps", () => {
      const politica = new PoliticaParticularSolo();
      const incidente = builderBase()
        .conUbicacion(4.60971, -74.08175)
        .construir();

      const contexto: ContextoConductor = {
        conductorId: "COND-TEST",
        nombre: "Juan Pérez",
        tipoPerfil: TipoPerfilConductor.PARTICULAR_SOLO,
        contactosEmergencia: [
          { nombre: "Contacto 1", telefono: "+573001112233", relacion: "Cónyuge" },
          { nombre: "Contacto 2", telefono: "+573004445566", relacion: "Hermano" }
        ]
      };

      const acciones = politica.determinarAcciones(incidente, contexto);

      expect(acciones).toHaveLength(2);
      expect(acciones[0].canal).toBe(CanalNotificacion.SMS);
      expect(acciones[0].destinatario).toBe("+573001112233");
      expect(acciones[0].mensaje).toContain("Juan Pérez");
      expect(acciones[0].mensaje).toContain("MICROSUEÑO");
      expect(acciones[0].mensaje).toContain("https://maps.google.com/?q=4.60971,-74.08175");

      expect(acciones[1].destinatario).toBe("+573004445566");
    });

    it("PoliticaParticularSolo debe indicar túnel/sin cobertura si el incidente no tiene GPS", () => {
      const politica = new PoliticaParticularSolo();
      const incidente = builderBase().sinUbicacion().construir();

      const contexto: ContextoConductor = {
        conductorId: "COND-TEST",
        nombre: "Juan Pérez",
        tipoPerfil: TipoPerfilConductor.PARTICULAR_SOLO,
        contactosEmergencia: [
          { nombre: "Contacto 1", telefono: "+573001112233" }
        ]
      };

      const acciones = politica.determinarAcciones(incidente, contexto);
      expect(acciones).toHaveLength(1);
      expect(acciones[0].mensaje).toContain("Ubicación GPS: No disponible (posible túnel o zona sin cobertura)");
    });

    it("PoliticaParticularConCopiloto debe despachar Push Notification FCM al copiloto", () => {
      const politica = new PoliticaParticularConCopiloto();
      const incidente = builderBase().construir();

      const contexto: ContextoConductor = {
        conductorId: "COND-TEST",
        nombre: "Ana Martínez",
        tipoPerfil: TipoPerfilConductor.PARTICULAR_CON_COPILOTO,
        copiloto: {
          nombre: "David Castro",
          tokenFcm: "token_fcm_copiloto_12345"
        }
      };

      const acciones = politica.determinarAcciones(incidente, contexto);

      expect(acciones).toHaveLength(1);
      expect(acciones[0].canal).toBe(CanalNotificacion.PUSH_FCM);
      expect(acciones[0].destinatario).toBe("token_fcm_copiloto_12345");
      expect(acciones[0].mensaje).toContain("David Castro");
      expect(acciones[0].mensaje).toContain("Ana Martínez");
      expect(acciones[0].metadatos?.prioridad).toBe("HIGH");
    });

    it("PoliticaFlota debe generar alerta telemática dirigida al dashboard del despachador B2B", () => {
      const politica = new PoliticaFlota();
      const incidente = builderBase()
        .conUbicacion(6.2442, -75.5812)
        .construir();

      const contexto: ContextoConductor = {
        conductorId: "COND-FLOTA-99",
        nombre: "Roberto Sánchez",
        tipoPerfil: TipoPerfilConductor.FLOTA,
        flota: {
          empresaId: "COL_LOGISTICA_SA",
          despachadorId: "DESPACHO_CENTRAL",
          canalDashboard: "dashboard-col-logistica",
          placaVehiculo: "TSA-123"
        }
      };

      const acciones = politica.determinarAcciones(incidente, contexto);

      expect(acciones).toHaveLength(1);
      expect(acciones[0].canal).toBe(CanalNotificacion.DASHBOARD_WEB);
      expect(acciones[0].destinatario).toBe("dashboard-col-logistica");
      expect(acciones[0].mensaje).toContain("TSA-123");
      expect(acciones[0].mensaje).toContain("Roberto Sánchez");
      expect(acciones[0].metadatos?.empresaId).toBe("COL_LOGISTICA_SA");
      expect(acciones[0].metadatos?.placaVehiculo).toBe("TSA-123");
    });
  });

  // ---------------------------------------------------------------------------
  // 3. PATRÓN FACTORY METHOD: FabricaPoliticas
  // ---------------------------------------------------------------------------
  describe("Patrón Factory Method: FabricaPoliticas", () => {
    let fabrica: FabricaPoliticas;

    beforeEach(() => {
      fabrica = new FabricaPoliticas();
    });

    it("debe instanciar PoliticaParticularSolo cuando el perfil es PARTICULAR_SOLO", () => {
      const contexto: ContextoConductor = {
        conductorId: "C1",
        nombre: "Test",
        tipoPerfil: TipoPerfilConductor.PARTICULAR_SOLO
      };
      const politica = fabrica.crearPara(contexto);
      expect(politica).toBeInstanceOf(PoliticaParticularSolo);
    });

    it("debe instanciar PoliticaParticularConCopiloto cuando el perfil es PARTICULAR_CON_COPILOTO", () => {
      const contexto: ContextoConductor = {
        conductorId: "C2",
        nombre: "Test",
        tipoPerfil: TipoPerfilConductor.PARTICULAR_CON_COPILOTO
      };
      const politica = fabrica.crearPara(contexto);
      expect(politica).toBeInstanceOf(PoliticaParticularConCopiloto);
    });

    it("debe instanciar PoliticaFlota cuando el perfil es FLOTA", () => {
      const contexto: ContextoConductor = {
        conductorId: "C3",
        nombre: "Test",
        tipoPerfil: TipoPerfilConductor.FLOTA
      };
      const politica = fabrica.crearPara(contexto);
      expect(politica).toBeInstanceOf(PoliticaFlota);
    });

    it("debe lanzar un error descriptivo si el perfil no es reconocido", () => {
      const contexto: ContextoConductor = {
        conductorId: "C4",
        nombre: "Test",
        tipoPerfil: "PERFIL_INEXISTENTE"
      };
      expect(() => fabrica.crearPara(contexto)).toThrow("No se encontró una política de escalamiento");
    });
  });
});
