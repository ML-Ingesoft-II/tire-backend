import { ConfiguracionAlertService } from "./configuracion/ConfiguracionAlertService";
import { TipoEvento } from "./dominio/tipos";

/**
 * Script de Demostración Ejecutable del Microservicio 'tire_alert_service'
 * Proyecto Tire - Universidad Nacional de Colombia (2026-2)
 * 
 * Ilustra de manera interactiva y exhaustiva el funcionamiento de los 6 patrones GoF,
 * la arquitectura hexagonal, las políticas de escalamiento y los mecanismos de resiliencia.
 */
async function ejecutarDemostracion(): Promise<void> {
  console.log("===============================================================================");
  console.log("   SISTEMA TIRE: tire_alert_service - DEMOSTRACIÓN DE ARQUITECTURA Y PATRONES   ");
  console.log("===============================================================================\n");

  const config = new ConfiguracionAlertService({
    maxReintentos: 3,
    retrasoBaseMs: 10,
    ventanaMinutosRecientes: 15
  });

  // -----------------------------------------------------------------------------------------
  // ESCENARIO 1: Conductor Particular Solo con GPS disponible
  // Patrón Strategy (PoliticaParticularSolo) + Builder + State + Adapter (SMS)
  // -----------------------------------------------------------------------------------------
  console.log("▶ [ESCENARIO 1] Conductor Particular Solo (COND-001) - Alerta NO silenciada con GPS");
  console.log("  Contexto: Carlos Gómez viaja solo en vía abierta. MediaPipe detecta microsueño.");
  console.log("  Resultado esperado: Escala a CRÍTICO y despacha SMS a contactos de emergencia con Google Maps.");

  const respuesta1 = await config.controladorAlertasRest.procesarAlerta({
    body: {
      conductorId: "COND-001",
      tipoEvento: TipoEvento.MICROSUEÑO,
      silenciadoEnCabina: false, // ¡No silenciado! Conductor inconsciente o dormido
      ubicacion: {
        latitud: 4.60971,
        longitud: -74.08175,
        altitud: 2600
      }
    }
  });

  console.log("  Status HTTP:", respuesta1.status);
  console.log("  Respuesta:", JSON.stringify(respuesta1.body, null, 2));
  console.log("  Historial SMS enviados:", config.proveedorSms.obtenerHistorial().length, "mensajes");
  config.proveedorSms.obtenerHistorial().forEach((sms, idx) => {
    console.log(`    [SMS #${idx + 1}] Para: ${sms.destinatario} | Texto: ${sms.contenido}`);
  });
  console.log("\n-------------------------------------------------------------------------------\n");

  // -----------------------------------------------------------------------------------------
  // ESCENARIO 2: Conductor Particular Solo en Túnel (Sin GPS)
  // Patrón Builder (manejo seguro de opcionales) + Strategy (Formato sin GPS)
  // -----------------------------------------------------------------------------------------
  console.log("▶ [ESCENARIO 2] Conductor Particular Solo (COND-001) - Túnel de La Línea (Sin señal GPS)");
  console.log("  Contexto: Pérdida total de cobertura satelital. Alarma no silenciada.");
  console.log("  Resultado esperado: Builder crea el incidente sin GPS y la política indica zona de túnel.");

  config.proveedorSms.limpiar();

  const respuesta2 = await config.controladorAlertasRest.procesarAlerta({
    body: {
      conductorId: "COND-001",
      tipoEvento: TipoEvento.BOSTEZO_REPETITIVO,
      silenciadoEnCabina: false
      // Sin campo ubicación
    }
  });

  console.log("  Status HTTP:", respuesta2.status);
  config.proveedorSms.obtenerHistorial().forEach((sms, idx) => {
    console.log(`    [SMS #${idx + 1}] Para: ${sms.destinatario} | Texto: ${sms.contenido}`);
  });
  console.log("\n-------------------------------------------------------------------------------\n");

  // -----------------------------------------------------------------------------------------
  // ESCENARIO 3: Conductor Particular con Copiloto
  // Patrón Strategy (PoliticaParticularConCopiloto) + Adapter (Firebase FCM)
  // -----------------------------------------------------------------------------------------
  console.log("▶ [ESCENARIO 3] Conductor Particular con Copiloto (COND-002)");
  console.log("  Contexto: Mariana Rivas con copiloto Andrés Silva. Fatiga no silenciada.");
  console.log("  Resultado esperado: Despacha Push Notification de alta prioridad al copiloto.");

  const respuesta3 = await config.controladorAlertasRest.procesarAlerta({
    body: {
      conductorId: "COND-002",
      tipoEvento: TipoEvento.PARPADEO_LENTO,
      silenciadoEnCabina: false
    }
  });

  console.log("  Status HTTP:", respuesta3.status);
  const fcmHistorial = config.clienteFcm.obtenerHistorial();
  console.log("  Historial FCM enviados:", fcmHistorial.length, "notificaciones push");
  fcmHistorial.forEach((push, idx) => {
    console.log(`    [PUSH #${idx + 1}] Token: ${push.token}`);
    console.log(`    Título: ${push.payload.notification.title}`);
    console.log(`    Cuerpo: ${push.payload.notification.body}`);
  });
  console.log("\n-------------------------------------------------------------------------------\n");

  // -----------------------------------------------------------------------------------------
  // ESCENARIO 4: Conductor de Flota de Carga Crítica (B2B)
  // Patrón Strategy (PoliticaFlota) + Adapter (Dashboard Web)
  // -----------------------------------------------------------------------------------------
  console.log("▶ [ESCENARIO 4] Flota de Carga Crítica B2B (COND-003) - Javier Mendoza");
  console.log("  Contexto: Tractomula transportando carga peligrosa. Microsueño no silenciado.");
  console.log("  Resultado esperado: Alerta telemática al Dashboard Web del despachador de flota.");

  const respuesta4 = await config.controladorAlertasRest.procesarAlerta({
    body: {
      conductorId: "COND-003",
      tipoEvento: TipoEvento.MICROSUEÑO,
      silenciadoEnCabina: false,
      ubicacion: {
        latitud: 5.06889,
        longitud: -75.51738
      }
    }
  });

  console.log("  Status HTTP:", respuesta4.status);
  const dashboardHistorial = config.notificadorDashboardBase.obtenerHistorial();
  console.log("  Historial Dashboard:", dashboardHistorial.length, "alertas en panel");
  dashboardHistorial.forEach((alerta, idx) => {
    console.log(`    [DASHBOARD #${idx + 1}] Canal: ${alerta.canalDashboard}`);
    console.log(`    Mensaje: ${alerta.mensaje}`);
  });
  console.log("\n-------------------------------------------------------------------------------\n");

  // -----------------------------------------------------------------------------------------
  // ESCENARIO 5: Alertas Silenciadas pero Superando Umbral de Repetición
  // Lógica de Negocio Pura (ReglasEscalamiento): (!silenciado || repetidas >= umbral)
  // -----------------------------------------------------------------------------------------
  console.log("▶ [ESCENARIO 5] Evaluación de Umbral de Fatiga Crónica Acumulada (ReglasEscalamiento)");
  console.log("  Contexto: Un conductor silencia las alarmas en cabina, pero sigue durmiéndose.");
  console.log("  Umbral configurado: 3 eventos en la ventana de 15 minutos.");

  // Limpiamos repositorio para demostrar exactamente la progresión de fatiga acumulada
  await config.repositorioIncidentes.limpiar();

  // Alerta 1: Silenciada (Eventos previos = 0). No debe escalar.
  console.log("  -> Enviando Alerta 1 (Silenciada, eventos previos en ventana: 0)...");
  const rSilenciada1 = await config.controladorAlertasRest.procesarAlerta({
    body: {
      conductorId: "COND-001",
      tipoEvento: TipoEvento.BOSTEZO_REPETITIVO,
      silenciadoEnCabina: true
    }
  });
  const res1 = (rSilenciada1.body as any).resultado;
  console.log(`     Escalado: ${res1.escalado} | Estado: ${res1.estadoFinal} | Nivel: ${res1.nivelRiesgoFinal}`);

  // Alerta 2: Silenciada (Eventos previos = 1). No debe escalar.
  console.log("  -> Enviando Alerta 2 (Silenciada, eventos previos en ventana: 1)...");
  const rSilenciada2 = await config.controladorAlertasRest.procesarAlerta({
    body: {
      conductorId: "COND-001",
      tipoEvento: TipoEvento.PARPADEO_LENTO,
      silenciadoEnCabina: true
    }
  });
  const res2 = (rSilenciada2.body as any).resultado;
  console.log(`     Escalado: ${res2.escalado} | Estado: ${res2.estadoFinal} | Nivel: ${res2.nivelRiesgoFinal}`);

  // Alerta 3: Silenciada (Eventos previos = 2). Aún no alcanza umbral (2 < 3). No debe escalar.
  console.log("  -> Enviando Alerta 3 (Silenciada, eventos previos en ventana: 2)...");
  const rSilenciada3 = await config.controladorAlertasRest.procesarAlerta({
    body: {
      conductorId: "COND-001",
      tipoEvento: TipoEvento.POSTURA_INADECUADA,
      silenciadoEnCabina: true
    }
  });
  const res3 = (rSilenciada3.body as any).resultado;
  console.log(`     Escalado: ${res3.escalado} | Estado: ${res3.estadoFinal} | Nivel: ${res3.nivelRiesgoFinal}`);

  // Alerta 4: Silenciada (Eventos previos = 3 >= umbral 3). ¡Debe escalar por fatiga crónica!
  console.log("  -> Enviando Alerta 4 (Silenciada, eventos previos en ventana: 3 >= umbral 3)...");
  const rSilenciada4 = await config.controladorAlertasRest.procesarAlerta({
    body: {
      conductorId: "COND-001",
      tipoEvento: TipoEvento.MICROSUEÑO,
      silenciadoEnCabina: true
    }
  });
  const res4 = (rSilenciada4.body as any).resultado;
  console.log(`     Escalado: ${res4.escalado} | Estado: ${res4.estadoFinal} | Nivel: ${res4.nivelRiesgoFinal}`);
  console.log(`     Motivo: ${res4.motivoEscalamiento}`);
  console.log("\n-------------------------------------------------------------------------------\n");

  // -----------------------------------------------------------------------------------------
  // ESCENARIO 6: Resiliencia con Reintentos Exponenciales (Patrón Decorator)
  // NotificadorConReintento recuperando una falla transitoria de red
  // -----------------------------------------------------------------------------------------
  console.log("▶ [ESCENARIO 6] Resiliencia ante Fallos de Red Transitorios (Patrón Decorator)");
  console.log("  Contexto: El proveedor FCM sufre 2 caídas de conexión simuladas consecutivas.");
  console.log("  Resultado esperado: NotificadorConReintento reintenta automáticamente y logra el envío en el 3er intento.");

  config.clienteFcm.limpiar();
  config.clienteFcm.configurarFallasTransitorias(2); // Fallará en intento 1 y 2, tendrá éxito en 3

  const respuestaResiliencia = await config.controladorAlertasRest.procesarAlerta({
    body: {
      conductorId: "COND-002",
      tipoEvento: TipoEvento.MICROSUEÑO,
      silenciadoEnCabina: false
    }
  });

  const resResiliente = (respuestaResiliencia.body as any).resultado;
  console.log("  Resultado del envío:", resResiliente.notificacionesDespachadas[0]);
  console.log(`  ¿Éxito final?: ${resResiliente.notificacionesDespachadas[0].exitoso}`);
  console.log(`  Reintentos ejecutados: ${resResiliente.notificacionesDespachadas[0].reintentos}`);
  console.log("\n-------------------------------------------------------------------------------\n");

  // -----------------------------------------------------------------------------------------
  // ESCENARIO 7: Garantía Transaccional de Persistencia (AC06)
  // try ... finally asegurando persistencia incluso si el despacho colapsa
  // -----------------------------------------------------------------------------------------
  console.log("▶ [ESCENARIO 7] Garantía Transaccional y Auditoría Forense (Requerimiento AC06)");
  console.log("  Contexto: Falla catastrófica de red (más fallas que los reintentos permitidos).");
  console.log("  Resultado esperado: El incidente queda registrado en el repositorio con estado ESCALADO para auditoría legal.");

  config.proveedorSms.limpiar();
  config.proveedorSms.configurarFallasTransitorias(10); // Excederá el máximo de 3 reintentos

  const idIncidenteFalla = `INC-AUDIT-${Date.now()}`;
  const respuestaFalla = await config.controladorAlertasRest.procesarAlerta({
    body: {
      incidenteId: idIncidenteFalla,
      conductorId: "COND-001",
      tipoEvento: TipoEvento.MICROSUEÑO,
      silenciadoEnCabina: false
    }
  });

  const resAudit = (respuestaFalla.body as any).resultado;
  console.log("  Persistencia garantizada (persistenciaExitosa):", resAudit.persistenciaExitosa);

  const incidentePersistido = await config.repositorioIncidentes.buscarPorId(idIncidenteFalla);
  console.log("  Incidente verificado en Repositorio:");
  console.log(`    ID: ${incidentePersistido?.id}`);
  console.log(`    Estado: ${incidentePersistido?.obtenerNombreEstado()}`);
  console.log(`    Riesgo: ${incidentePersistido?.obtenerNivelRiesgo()}`);
  console.log(`    Transiciones en historial: ${incidentePersistido?.obtenerHistorial().length}`);

  console.log("\n===============================================================================");
  console.log("   DEMOSTRACIÓN FINALIZADA CON ÉXITO: 100% REQUERIMIENTOS Y PATRONES CUMPLIDOS   ");
  console.log("===============================================================================");
}

ejecutarDemostracion().catch(err => {
  console.error("Error crítico en demostración:", err);
  process.exit(1);
});
