import { ConfiguracionAlertService } from "../src/configuracion/ConfiguracionAlertService";
import { TipoEvento } from "../src/dominio/tipos";

describe("Adaptador de Entrada: ControladorAlertasRest (Validaciones HTTP)", () => {
  let config: ConfiguracionAlertService;

  beforeEach(async () => {
    config = new ConfiguracionAlertService({
      maxReintentos: 2,
      retrasoBaseMs: 5
    });
    await config.reiniciar();
  });

  it("debe retornar 400 si el body está vacío o no es un objeto", async () => {
    const respuesta1 = await config.controladorAlertasRest.procesarAlerta({
      body: null
    });
    expect(respuesta1.status).toBe(400);
    expect((respuesta1.body as any).error).toBe("BAD_REQUEST");

    const respuesta2 = await config.controladorAlertasRest.procesarAlerta({
      body: undefined
    });
    expect(respuesta2.status).toBe(400);
  });

  it("debe retornar 400 si falta conductorId o es una cadena vacía", async () => {
    const respuesta = await config.controladorAlertasRest.procesarAlerta({
      body: {
        tipoEvento: TipoEvento.MICROSUEÑO,
        silenciadoEnCabina: false
      }
    });
    expect(respuesta.status).toBe(400);
    expect((respuesta.body as any).campo).toBe("conductorId");
  });

  it("debe retornar 400 si falta tipoEvento o es inválido", async () => {
    // Falta tipoEvento
    const resp1 = await config.controladorAlertasRest.procesarAlerta({
      body: {
        conductorId: "COND-001",
        silenciadoEnCabina: false
      }
    });
    expect(resp1.status).toBe(400);
    expect((resp1.body as any).campo).toBe("tipoEvento");

    // tipoEvento inválido
    const resp2 = await config.controladorAlertasRest.procesarAlerta({
      body: {
        conductorId: "COND-001",
        tipoEvento: "EVENTO_TOTALMENTE_INVALIDO",
        silenciadoEnCabina: false
      }
    });
    expect(resp2.status).toBe(400);
    expect((resp2.body as any).error).toBe("VALOR_INVALIDO");
  });

  it("debe retornar 400 si falta silenciadoEnCabina o no es booleano", async () => {
    const respuesta = await config.controladorAlertasRest.procesarAlerta({
      body: {
        conductorId: "COND-001",
        tipoEvento: TipoEvento.MICROSUEÑO
      }
    });
    expect(respuesta.status).toBe(400);
    expect((respuesta.body as any).campo).toBe("silenciadoEnCabina");
  });

  it("debe retornar 400 si las coordenadas GPS están fuera de rango", async () => {
    // Latitud fuera de rango (> 90)
    const respLat = await config.controladorAlertasRest.procesarAlerta({
      body: {
        conductorId: "COND-001",
        tipoEvento: TipoEvento.MICROSUEÑO,
        silenciadoEnCabina: false,
        ubicacion: {
          latitud: 120.5,
          longitud: -74.0
        }
      }
    });
    expect(respLat.status).toBe(400);
    expect((respLat.body as any).campo).toBe("ubicacion.latitud");

    // Longitud fuera de rango (< -180)
    const respLng = await config.controladorAlertasRest.procesarAlerta({
      body: {
        conductorId: "COND-001",
        tipoEvento: TipoEvento.MICROSUEÑO,
        silenciadoEnCabina: false,
        ubicacion: {
          latitud: 4.6,
          longitud: -195.0
        }
      }
    });
    expect(respLng.status).toBe(400);
    expect((respLng.body as any).campo).toBe("ubicacion.longitud");
  });

  it("debe retornar 404 si el conductor no existe en el sistema", async () => {
    const respuesta = await config.controladorAlertasRest.procesarAlerta({
      body: {
        conductorId: "COND-NO-EXISTE-999",
        tipoEvento: TipoEvento.MICROSUEÑO,
        silenciadoEnCabina: false
      }
    });
    expect(respuesta.status).toBe(404);
    expect((respuesta.body as any).error).toBe("NO_ENCONTRADO");
    expect((respuesta.body as any).mensaje).toContain("no encontrado en el sistema");
  });

  it("debe retornar 200 con el resultado procesado para un payload válido", async () => {
    const respuesta = await config.controladorAlertasRest.procesarAlerta({
      body: {
        conductorId: "COND-001",
        tipoEvento: TipoEvento.MICROSUEÑO,
        silenciadoEnCabina: false,
        ubicacion: {
          latitud: 4.60971,
          longitud: -74.08175
        }
      }
    });

    expect(respuesta.status).toBe(200);
    const resultado = (respuesta.body as any).resultado;
    expect(resultado.conductorId).toBe("COND-001");
    expect(resultado.escalado).toBe(true);
    expect(resultado.estadoFinal).toBe("ESCALADO");
    expect(resultado.persistenciaExitosa).toBe(true);
  });
});
