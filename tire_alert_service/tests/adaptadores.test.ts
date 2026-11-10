import { ClienteFcmFalso } from "../src/adaptadores/salida/externos/ClienteFcmFalso";
import { ProveedorSmsFalso } from "../src/adaptadores/salida/externos/ProveedorSmsFalso";
import { NotificadorPushFcmSimulado } from "../src/adaptadores/salida/notificadores/NotificadorPushFcmSimulado";
import { NotificadorSmsSimulado } from "../src/adaptadores/salida/notificadores/NotificadorSmsSimulado";
import { NotificadorDashboardSimulado } from "../src/adaptadores/salida/notificadores/NotificadorDashboardSimulado";
import { NotificadorConReintento } from "../src/adaptadores/salida/notificadores/NotificadorConReintento";
import { RepositorioIncidentesEnMemoria } from "../src/adaptadores/salida/RepositorioIncidentesEnMemoria";
import { IncidenteBuilder } from "../src/dominio/incidente/IncidenteBuilder";
import { AccionNotificacion, CanalNotificacion, TipoEvento } from "../src/dominio/tipos";

describe("Adaptadores de Infraestructura: Adapter, Decorator y Repositorio", () => {
  // ---------------------------------------------------------------------------
  // 1. PATRÓN ADAPTER: NotificadorPushFcmSimulado y NotificadorSmsSimulado
  // ---------------------------------------------------------------------------
  describe("Patrón Adapter: Adaptadores hacia Clientes Externos (Adaptees)", () => {
    it("NotificadorPushFcmSimulado debe adaptar la llamada de dominio a ClienteFcmFalso.send", async () => {
      const clienteFcm = new ClienteFcmFalso();
      const adaptador = new NotificadorPushFcmSimulado(clienteFcm);

      expect(adaptador.soporta(CanalNotificacion.PUSH_FCM)).toBe(true);
      expect(adaptador.soporta(CanalNotificacion.SMS)).toBe(false);

      const accion: AccionNotificacion = {
        canal: CanalNotificacion.PUSH_FCM,
        destinatario: "token_fcm_copiloto_abc",
        mensaje: "¡Alerta copiloto!",
        metadatos: { prioridad: "HIGH" }
      };

      const resultado = await adaptador.enviar(accion);

      expect(resultado.exitoso).toBe(true);
      expect(resultado.canal).toBe(CanalNotificacion.PUSH_FCM);
      expect(resultado.destinatario).toBe("token_fcm_copiloto_abc");

      // Verificamos que el Adaptee recibió la invocación adaptada
      const historial = clienteFcm.obtenerHistorial();
      expect(historial).toHaveLength(1);
      expect(historial[0].token).toBe("token_fcm_copiloto_abc");
      expect(historial[0].payload.notification.body).toBe("¡Alerta copiloto!");
    });

    it("NotificadorSmsSimulado debe adaptar la llamada de dominio a ProveedorSmsFalso.sendMessage", async () => {
      const proveedorSms = new ProveedorSmsFalso();
      const adaptador = new NotificadorSmsSimulado(proveedorSms);

      expect(adaptador.soporta(CanalNotificacion.SMS)).toBe(true);
      expect(adaptador.soporta(CanalNotificacion.PUSH_FCM)).toBe(false);

      const accion: AccionNotificacion = {
        canal: CanalNotificacion.SMS,
        destinatario: "+573009998877",
        mensaje: "Alerta SMS contactos"
      };

      const resultado = await adaptador.enviar(accion);

      expect(resultado.exitoso).toBe(true);
      expect(resultado.canal).toBe(CanalNotificacion.SMS);

      // Verificamos el Adaptee
      const historial = proveedorSms.obtenerHistorial();
      expect(historial).toHaveLength(1);
      expect(historial[0].destinatario).toBe("+573009998877");
      expect(historial[0].contenido).toBe("Alerta SMS contactos");
    });

    it("NotificadorDashboardSimulado debe registrar eventos para el canal DASHBOARD_WEB", async () => {
      const adaptador = new NotificadorDashboardSimulado();
      expect(adaptador.soporta(CanalNotificacion.DASHBOARD_WEB)).toBe(true);

      const accion: AccionNotificacion = {
        canal: CanalNotificacion.DASHBOARD_WEB,
        destinatario: "dashboard-flota-central",
        mensaje: "Alerta despacho flota"
      };

      const resultado = await adaptador.enviar(accion);
      expect(resultado.exitoso).toBe(true);
      expect(adaptador.obtenerHistorial()).toHaveLength(1);
    });
  });

  // ---------------------------------------------------------------------------
  // 2. PATRÓN DECORATOR: NotificadorConReintento
  // ---------------------------------------------------------------------------
  describe("Patrón Decorator: NotificadorConReintento (Resiliencia transitoria)", () => {
    it("debe completar exitosamente en el primer intento si no hay fallas (0 reintentos)", async () => {
      const clienteFcm = new ClienteFcmFalso();
      const adaptadorBase = new NotificadorPushFcmSimulado(clienteFcm);
      const decorador = new NotificadorConReintento(adaptadorBase, 3, 5);

      const accion: AccionNotificacion = {
        canal: CanalNotificacion.PUSH_FCM,
        destinatario: "token_fcm_1",
        mensaje: "Prueba sin falla"
      };

      const resultado = await decorador.enviar(accion);

      expect(resultado.exitoso).toBe(true);
      expect(resultado.reintentos).toBe(0);
      expect(clienteFcm.obtenerHistorial()).toHaveLength(1);
    });

    it("debe recuperarse tras 2 fallas transitorias en el 3er intento", async () => {
      const clienteFcm = new ClienteFcmFalso();
      // Configuramos 2 fallas transitorias de red
      clienteFcm.configurarFallasTransitorias(2);

      const adaptadorBase = new NotificadorPushFcmSimulado(clienteFcm);
      const decorador = new NotificadorConReintento(adaptadorBase, 3, 5);

      const accion: AccionNotificacion = {
        canal: CanalNotificacion.PUSH_FCM,
        destinatario: "token_fcm_recuperacion",
        mensaje: "Prueba recuperación"
      };

      const resultado = await decorador.enviar(accion);

      expect(resultado.exitoso).toBe(true);
      expect(resultado.reintentos).toBe(2); // Falló 2 veces y tuvo éxito en la 3ra
      expect(clienteFcm.obtenerHistorial()).toHaveLength(1);
    });

    it("debe reportar fallo controlado cuando las fallas exceden el máximo de reintentos configurado", async () => {
      const proveedorSms = new ProveedorSmsFalso();
      // 5 fallas consecutivas, pero el decorador sólo intentará 3 veces
      proveedorSms.configurarFallasTransitorias(5);

      const adaptadorBase = new NotificadorSmsSimulado(proveedorSms);
      const decorador = new NotificadorConReintento(adaptadorBase, 3, 5);

      const accion: AccionNotificacion = {
        canal: CanalNotificacion.SMS,
        destinatario: "+573001234567",
        mensaje: "Prueba agotamiento"
      };

      const resultado = await decorador.enviar(accion);

      expect(resultado.exitoso).toBe(false);
      expect(resultado.reintentos).toBe(2); // 3 intentos en total (intento inicial + 2 reintentos)
      expect(resultado.error).toContain("Agotados 3 intentos");
    });
  });

  // ---------------------------------------------------------------------------
  // 3. REPOSITORIO DE INCIDENTES EN MEMORIA
  // ---------------------------------------------------------------------------
  describe("Repositorio de Incidentes en Memoria", () => {
    let repo: RepositorioIncidentesEnMemoria;

    beforeEach(async () => {
      repo = new RepositorioIncidentesEnMemoria();
    });

    it("debe guardar y recuperar un incidente por ID", async () => {
      const inc = new IncidenteBuilder()
        .conId("INC-TEST-REPO-01")
        .conConductorId("COND-100")
        .conTipoEvento(TipoEvento.MICROSUEÑO)
        .conSilenciadoEnCabina(false)
        .construir();

      await repo.guardar(inc);

      const recuperado = await repo.buscarPorId("INC-TEST-REPO-01");
      expect(recuperado).not.toBeNull();
      expect(recuperado?.id).toBe("INC-TEST-REPO-01");
      expect(recuperado?.conductorId).toBe("COND-100");
    });

    it("debe contar correctamente eventos recientes dentro de la ventana de tiempo especificada", async () => {
      const ahora = Date.now();
      const conductorId = "COND-200";

      // 1. Incidente de hace 5 minutos (DENTRO de ventana de 15 min)
      const inc1 = new IncidenteBuilder()
        .conConductorId(conductorId)
        .conTipoEvento(TipoEvento.BOSTEZO_REPETITIVO)
        .conTimestamp(new Date(ahora - 5 * 60 * 1000))
        .conSilenciadoEnCabina(true)
        .construir();

      // 2. Incidente de hace 10 minutos (DENTRO de ventana de 15 min)
      const inc2 = new IncidenteBuilder()
        .conConductorId(conductorId)
        .conTipoEvento(TipoEvento.PARPADEO_LENTO)
        .conTimestamp(new Date(ahora - 10 * 60 * 1000))
        .conSilenciadoEnCabina(true)
        .construir();

      // 3. Incidente de hace 30 minutos (FUERA de ventana de 15 min)
      const inc3 = new IncidenteBuilder()
        .conConductorId(conductorId)
        .conTipoEvento(TipoEvento.MICROSUEÑO)
        .conTimestamp(new Date(ahora - 30 * 60 * 1000))
        .conSilenciadoEnCabina(true)
        .construir();

      // 4. Incidente de hace 2 minutos pero de OTRO conductor
      const incOtro = new IncidenteBuilder()
        .conConductorId("COND-OTRO")
        .conTipoEvento(TipoEvento.MICROSUEÑO)
        .conTimestamp(new Date(ahora - 2 * 60 * 1000))
        .conSilenciadoEnCabina(true)
        .construir();

      await repo.guardar(inc1);
      await repo.guardar(inc2);
      await repo.guardar(inc3);
      await repo.guardar(incOtro);

      const cuentaRecientes = await repo.contarRecientes(conductorId, 15);
      expect(cuentaRecientes).toBe(2); // Solo inc1 e inc2
    });
  });
});
