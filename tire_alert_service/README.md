# Tire — tire_alert_service

> **Componente Central de Orquestación y Escalamiento de Alertas de Fatiga y Microsueños**  
> Laboratorio de Patrones de Diseño y Arquitectura de Software  
> **Universidad Nacional de Colombia (2026-2)**

---

## 1. Visión General del Sistema y Dominio

El servicio `tire_alert_service` es el componente neurálgico del backend del sistema **Tire**, una plataforma proactiva para mitigar siniestros viales causados por fatiga y microsueños en conductores de transporte de carga pesada y vehículos particulares (responsables de cerca del 28% de los accidentes fatales según la Agencia Nacional de Seguridad Vial - ANSV).

```
   ┌────────────────────────────────────────────────────────┐
   │                  CABINA DEL VEHÍCULO                   │
   │  ┌─────────────────────────┐  Alarma Sonora / Visual   │
   │  │   Edge AI / MediaPipe   │ ────────────────────────> │
   │  └────────────┬────────────┘                           │
   └───────────────┼────────────────────────────────────────┘
                   │ Evento telemático no resuelto / fatiga acumulada
                   ▼
   ┌────────────────────────────────────────────────────────┐
   │            tire_alert_service (TypeScript)             │
   │                                                        │
   │  1. Evalúa reglas de escalamiento (!silenciado || >=3) │
   │  2. Muta ciclo de vida (State: DETECTADO -> ESCALADO)  │
   │  3. Selecciona estrategia (Factory Method + Strategy)  │
   │  4. Despacha vía adaptadores decorados con reintento   │
   │  5. Persistencia obligatoria en Repositorio (AC06)     │
   └───────────┬───────────────┬────────────────┬───────────┘
               │               │                │
          Canal SMS       Canal PUSH     Canal DASHBOARD
               │               │                │
               ▼               ▼                ▼
        Contactos de       Copiloto        Despachador B2B
         Emergencia       (Acompañante)     (Centro Flota)
```

---

## 2. Arquitectura Hexagonal (Puertos y Adaptadores)

El componente está diseñado bajo una estricta **Arquitectura Hexagonal (Puertos y Adaptadores)**:

```
src/
  dominio/                          # Reglas de negocio puras (cero frameworks)
    tipos.ts                        # Enums, interfaces y contratos de dominio
    ReglasEscalamiento.ts           # Lógica pura: (!silenciado || repetidas >= umbral)
    incidente/
      Incidente.ts                  # Entidad central (Context del patrón State)
      IncidenteBuilder.ts           # Creación validada (patrón Builder)
      estados/                      # Patrón State
        EstadoIncidente.ts          # Interfaz State
        EstadoDetectado.ts          # Estado inicial (Nivel: PRECAUCION)
        EstadoEscalado.ts           # Estado crítico (Nivel: CRITICO)
        EstadoAtendido.ts           # Estado terminal (Nivel: NORMAL)
    politicas/                      # Patrón Strategy
      PoliticaEscalamiento.ts       # Interfaz Strategy
      PoliticaParticularSolo.ts     # ConcreteStrategy: SMS con GPS o túnel
      PoliticaParticularConCopiloto.ts # ConcreteStrategy: Push FCM a copiloto
      PoliticaFlota.ts              # ConcreteStrategy: Dashboard a despachador B2B
  aplicacion/                       # Casos de uso y orquestación
    ServicioEscalamiento.ts         # Caso de uso central (Context de Strategy y garantía AC06)
    FabricaPoliticas.ts             # Patrón Factory Method (instanciación desacoplada)
    puertos/
      entrada/
        ProcesarAlerta.ts           # Puerto de entrada primario
      salida/
        ConsultorPerfil.ts          # Puerto de salida para contexto de conductor
        RepositorioIncidentes.ts    # Puerto de persistencia y auditoría forense
        Notificador.ts              # Puerto abstracto para despacho multicanal
  adaptadores/                      # Conexión con infraestructura
    entrada/
      ControladorAlertasRest.ts     # Adaptador primario REST HTTP con validación
    salida/
      notificadores/
        NotificadorPushFcmSimulado.ts # Adapter hacia Google FCM
        NotificadorSmsSimulado.ts   # Adapter hacia Gateway SMS (Twilio/Sinch)
        NotificadorDashboardSimulado.ts # Adapter hacia WebSocket/Dashboard Web
        NotificadorConReintento.ts  # Patrón Decorator (resiliencia con reintentos)
      RepositorioIncidentesEnMemoria.ts # Implementación volátil de persistencia
      ConsultorPerfilSimulado.ts    # Stub con perfiles canónicos
      externos/                     # Clientes simulados ("Adaptees")
        ClienteFcmFalso.ts          # Simula SDK oficial de Google FCM
        ProveedorSmsFalso.ts        # Simula API de mensajería SMS
  configuracion/
    ConfiguracionAlertService.ts    # Composition Root / Inyector de dependencias
  main.ts                           # Script interactivo de demostración
tests/
  nucleo.test.ts                    # Tests de Strategy, Factory y ReglasEscalamiento
  incidente.test.ts                 # Tests de State y Builder
  adaptadores.test.ts               # Tests de Adapter, Decorator y Repositorio
  controlador.test.ts               # Tests de validación HTTP REST
  abierto-cerrado.test.ts           # Demostración del principio OCP (Transporte Público)
```

---

## 3. Catálogo de los 6 Patrones de Diseño GoF

| # | Patrón | Categoría GoF | Clase Principal | Problema Resuelto en Tire | Principios SOLID |
|---|---|---|---|---|---|
| 1 | **Factory Method** | Creacional | `FabricaPoliticas` | El caso de uso no debe acoplarse con `new` a las estrategias de escalamiento según el tipo de conductor. | SRP, OCP |
| 2 | **Builder** | Creacional | `IncidenteBuilder` | Construcción paso a paso y validación exhaustiva de incidentes con parámetros opcionales (GPS en túneles). | SRP |
| 3 | **Adapter** | Estructural | `NotificadorPushFcmSimulado`, `NotificadorSmsSimulado` | Traduce las interfaces incompatibles de los SDKs externos (`send(token, payload)`, `sendMessage(to, text)`) al puerto `Notificador.enviar()`. | DIP |
| 4 | **Decorator** | Estructural | `NotificadorConReintento` | Incorpora tolerancia a fallos transitorios de red mediante reintentos exponenciales sin alterar los adaptadores base. | SRP, OCP |
| 5 | **Strategy** | Comportamiento | `PoliticaEscalamiento` y concretas | Encapsula algoritmos intercambiables para destinatarios, mensajes y canales (SMS, PUSH, DASHBOARD). | OCP |
| 6 | **State** | Comportamiento | `EstadoIncidente`, `EstadoDetectado`, `EstadoEscalado`, `EstadoAtendido` | Administra transiciones del ciclo de vida y cálculo de nivel de riesgo sin sentencias condicionales `switch(estado)` anidadas. | SRP, OCP |

---

## 4. Garantía Transaccional y Auditoría Forense (AC06)

En `ServicioEscalamiento.ts`, el guardado del incidente está confinado en un bloque `try ... finally`:

```typescript
try {
  if (debeEscalar) {
    incidente.escalar(motivo);
    const politica = this.fabricaPoliticas.crearPara(contexto);
    const acciones = politica.determinarAcciones(incidente, contexto);
    for (const accion of acciones) {
      await this.despacharNotificacion(accion);
    }
  }
} finally {
  // AC06: Persistencia incondicional en repositorio para bitácora forense legal
  await this.repositorio.guardar(incidente);
  persistenciaExitosa = true;
}
```

Esto garantiza que incluso si la red colapsa y todas las notificaciones externas fallan, el incidente queda registrado en la base de datos como evidencia jurídica del evento telemático.

---

## 5. Demostración del Principio Abierto/Cerrado (OCP - Fase 7)

El archivo `tests/abierto-cerrado.test.ts` demuestra cómo introducir el perfil `TRANSPORTE_PUBLICO` con `PoliticaTransportePublico` mediante dos mecanismos:
1. **Herencia (Factory Method clásico):** Sobreescribiendo `crearPoliticaConcreta` en una subclase de `FabricaPoliticas`.
2. **Registro Dinámico:** Registrando la factoría con `FabricaPoliticas.registrarPolitica("TRANSPORTE_PUBLICO", ...)`.

Ambos métodos operan **sin modificar ni una sola línea de código existente en `src/`**.

---

## 6. Puesta en Marcha y Verificación

### Instalación de dependencias
```bash
npm install
```

### Ejecutar demostración interactiva
```bash
npm start
```

### Ejecutar suite completa de tests (41 tests)
```bash
npm test
```

### Generar reporte de cobertura de código
```bash
npm run test:coverage
```

### Compilar TypeScript a JavaScript
```bash
npm run build
```
