# Tire — Backend (Prototipo 1)

**Tire** es un sistema que previene accidentes viales causados por **microsueños** en
conductores de transporte de carga y particulares. Detecta episodios de fatiga en
tiempo real, calcula un nivel de alerta y notifica a un despachador cuando una
situación se vuelve crítica.

Este repositorio contiene el **único backend** del sistema, que conecta:

- Una **App Móvil** (React Native) usada por el rol **CONDUCTOR**.
- Un **Panel Web** (React) usado por el rol **DESPACHADOR**.

El backend está construido en **Java 17 + Spring Boot 3.2.5** bajo **Arquitectura
Hexagonal (Puertos y Adaptadores)** estricta.

---

## Integrantes del equipo

- Deivid Farid Ardila Herrera
- Ángel David Beltrán García
- Óscar Iván Ulises Gutiérrez Palacios
- Juan Camilo Vergara Tao
- Julián Esteban Cadena Rojas

---

## Arquitectura Hexagonal

El código se organiza en tres capas concéntricas. La **regla de oro** es que el
paquete `domain` es **Java puro**: no importa anotaciones de Spring, JPA ni
Jackson. Las dependencias siempre apuntan hacia adentro (infraestructura →
aplicación → dominio).

### Mapa de paquetes

| Paquete | Capa | Responsabilidad |
|---|---|---|
| `com.unal.tire.domain.model` | Dominio | Entidades y agregado raíz (`Viaje`, `Conductor`, `Usuario`, `EventoFatiga`, `AlertaCritica`). |
| `com.unal.tire.domain.valueobject` | Dominio | Objetos de valor y enums (`Ubicacion`, `CondicionClimatica`, `RolUsuario`, `EstadoViaje`, `NivelAlerta`). |
| `com.unal.tire.domain.state` | Dominio | Patrón **State** del nivel de alerta (`EstadoAlertaState` y estados concretos). |
| `com.unal.tire.domain.exception` | Dominio | Excepciones puras de negocio (`ReglaNegocioException`, `ConflictoEstadoException`, `RecursoNoEncontradoException`, `AccesoDenegadoDominioException`). |
| `com.unal.tire.application.port.in` | Aplicación | Puertos de entrada (casos de uso) y comandos. |
| `com.unal.tire.application.port.out` | Aplicación | Puertos de salida (persistencia, autenticación, clima). |
| `com.unal.tire.application.service` | Aplicación | Implementación de los casos de uso (orquestación). |
| `com.unal.tire.infrastructure.adapter.in.rest` | Infraestructura | Controladores REST y DTOs (adaptadores de entrada). |
| `com.unal.tire.infrastructure.adapter.out.persistence` | Infraestructura | Entidades JPA, repositorios Spring Data y adaptadores de persistencia. |
| `com.unal.tire.infrastructure.adapter.out.external` | Infraestructura | Adaptador **Open-Meteo** para clima en ruta. |
| `com.unal.tire.infrastructure.security` | Infraestructura | Adaptador JWT/BCrypt, filtro, manejadores 401/403. |
| `com.unal.tire.infrastructure.config` | Infraestructura | `SecurityConfig` y `OpenApiConfig`. |

### Principios SOLID aplicados

- **S (Responsabilidad única):** cada caso de uso (`AutenticacionService`,
  `GestionConductoresService`, `GestionViajesService`, `GestionAlertasService`)
  cubre un objetivo acotado.
- **O (Abierto/Cerrado):** el nivel de alerta se extiende añadiendo nuevos estados
  (patrón State) sin modificar el agregado `Viaje`.
- **L (Sustitución de Liskov):** cualquier `EstadoAlertaState` es intercambiable
  donde se espera la interfaz.
- **I (Segregación de interfaces):** puertos pequeños y específicos
  (`UsuarioRepositoryPort`, `ConductorRepositoryPort`, `ConsultorClimaPort`, …).
- **D (Inversión de dependencias):** la capa de aplicación depende de **puertos**
  (interfaces), nunca de implementaciones concretas de infraestructura.

### Patrones de diseño (GoF)

- **State** (`com.unal.tire.domain.state`): modela las transiciones del nivel de
  alerta `NORMAL → PRECAUCION → CRITICO`. El agregado `Viaje` rehidrata el estado
  con `FabricaEstadoAlerta.desdeNivel(...)` y delega la transición al estado actual.
- **Adapter** (`com.unal.tire.infrastructure.security.JwtBcrypAuthAdapter`): adapta
  `BCryptPasswordEncoder` y la librería JJWT al puerto de dominio
  `ProveedorAutenticacionPort`. El mismo patrón se aplica en `OpenMeteoClimaAdapter`,
  que adapta la API externa de Open-Meteo al puerto `ConsultorClimaPort`.

---

## Stack tecnológico

- Java 17, Spring Boot 3.2.5 (Web, Validation, Security, Data JPA)
- PostgreSQL 16 (producción/desarrollo) · H2 en modo PostgreSQL (tests)
- JWT con JJWT 0.12.5 · BCrypt
- springdoc-openapi (Swagger UI) 2.5.0
- Errores bajo estándar **RFC 9457** (`application/problem+json`)

---

## Puesta en marcha en local

### Requisitos

- JDK 17
- Docker y Docker Compose
- (Opcional) Maven; el repositorio incluye el wrapper `./mvnw`.

### 1. Variables de entorno

Copia la plantilla y ajusta si lo necesitas:

```bash
cp .env.example .env
```

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `PORT` | Puerto HTTP del backend | `8080` |
| `DATABASE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/tire_db` |
| `DATABASE_USERNAME` | Usuario de la base de datos | `tire_user` |
| `DATABASE_PASSWORD` | Contraseña de la base de datos | `tire_password` |
| `JWT_SECRET` | Secreto HMAC-SHA para firmar los JWT | *(valor de ejemplo en `.env.example`)* |
| `JWT_EXPIRATION_MS` | Vigencia del token en milisegundos | `3600000` (1 h) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (CSV) | `http://localhost:5173,http://localhost:3000` |

> **Seguridad:** `.env` está en `.gitignore`. Nunca subas secretos al repositorio.
> En producción genera un `JWT_SECRET` propio y robusto.

### 2. Levantar PostgreSQL con Docker Compose

```bash
docker compose up -d
```

Esto inicia `postgres:16-alpine` con la base `tire_db`, usuario `tire_user` y
contraseña `tire_password`, publicando el puerto `5432`.

### 3. Ejecutar el backend

```bash
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8080`.

> El esquema se crea/actualiza automáticamente (`spring.jpa.hibernate.ddl-auto=update`).

### Alternativa: todo en contenedores

El `Dockerfile` es multi-stage (build con Maven + Temurin 17, runtime con JRE 17
Alpine). Para construir la imagen del backend:

```bash
docker build -t tire-backend .
```

---

## Documentación de la API

- **Especificación OpenAPI 3.0.3:** [`openapi.yaml`](./openapi.yaml)
  (también servida en `http://localhost:8080/openapi.yaml`).
- **Swagger UI:** <http://localhost:8080/swagger-ui.html>

En Swagger UI usa el botón **Authorize** e introduce el token (`bearerAuth`, formato
JWT) obtenido de `POST /auth/sesion` para probar los endpoints protegidos.

### Resumen de rutas

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/auth/registro` | Público | Registrar usuario |
| POST | `/auth/sesion` | Público | Iniciar sesión (devuelve JWT) |
| PUT | `/auth/contrasena` | Autenticado | Cambiar contraseña |
| POST/GET | `/conductores` | DESPACHADOR | Crear / listar conductores |
| GET/PATCH/DELETE | `/conductores/{id}` | DESPACHADOR | Consultar / actualizar / desactivar |
| POST | `/viajes` | CONDUCTOR | Iniciar viaje |
| GET | `/viajes`, `/viajes/{id}` | CONDUCTOR o DESPACHADOR | Listar / consultar |
| PATCH | `/viajes/{id}/finalizar` | CONDUCTOR | Finalizar viaje |
| DELETE | `/viajes/{id}` | CONDUCTOR o DESPACHADOR | Cancelar viaje |
| POST | `/viajes/{id}/telemetria` | CONDUCTOR | Registrar telemetría (+ clima) |
| POST | `/viajes/{id}/eventos` | CONDUCTOR | Registrar evento de fatiga |
| PATCH | `/viajes/{id}/confirmar-alerta` | CONDUCTOR | Confirmar alerta |
| GET | `/flota/estado` | DESPACHADOR | Estado de la flota |
| GET | `/alertas` | DESPACHADOR | Alertas abiertas |
| PATCH | `/alertas/{id}/atender` | DESPACHADOR | Atender alerta |
| GET | `/incidentes` | DESPACHADOR | Incidentes atendidos |

---

## Pruebas

### Pruebas automatizadas (JUnit 5 + MockMvc)

Los tests usan **H2 en modo PostgreSQL** (`application-test.yml`), por lo que
corren en cualquier máquina **sin necesidad de Docker ni PostgreSQL encendidos**:

```bash
./mvnw clean test
```

La suite `TireBackendIntegrationTest` cubre: registro, login JWT, CRUD de
conductores, ciclo de vida de viajes, **401** (sin token), **403** (rol cruzado),
**400** (validación), **409** (doble viaje activo — invariante HU-09), **404**
(recurso inexistente) y el **patrón State** (microsueño de 2.0 s no confirmado que
escala a `CRITICO` y abre una alerta para el despachador).

### Pruebas manuales (Postman Collection Runner)

En la carpeta `postman/` encontrarás:

- `Tire-Prototipo1.postman_collection.json` — 17 peticiones secuenciales con
  scripts `pm.test(...)`.
- `Tire-Local.postman_environment.json` — entorno con `baseUrl` y las variables de
  token/IDs que la colección rellena automáticamente.

Pasos:

1. Levanta el backend (`./mvnw spring-boot:run`) y PostgreSQL (`docker compose up -d`).
2. Importa la colección y el entorno en Postman.
3. Selecciona el entorno **Tire - Local**.
4. Abre el **Collection Runner**, elige la colección en orden y ejecuta.

La colección encadena los tokens y los IDs entre peticiones, validando los códigos
`201/200/400/401/403/409` y reglas de negocio como HU-09 y la escalada a `CRITICO`.

---

## HTTPS / TLS en producción

El backend **no termina TLS por sí mismo**; se despliega detrás de un **reverse
proxy** gestionado que ofrece HTTPS automático:

- **Render** y **Railway** exponen el servicio tras su balanceador con certificado
  TLS administrado. El tráfico externo viaja cifrado (`https://…`) hasta el proxy, y
  este reenvía la petición al contenedor por HTTP interno.
- Configura las variables de entorno (`DATABASE_URL`, `JWT_SECRET`,
  `CORS_ALLOWED_ORIGINS`, etc.) en el panel del proveedor; **no** las incluyas en la
  imagen.
- En `CORS_ALLOWED_ORIGINS` registra el dominio HTTPS real del Panel Web y de la App.
- Como la aplicación corre detrás de proxy, respeta las cabeceras `X-Forwarded-*`
  (puedes habilitar `server.forward-headers-strategy=framework` si necesitas que
  Spring reconstruya la URL original para enlaces absolutos).

Con este esquema, los clientes solo hablan HTTPS y el certificado lo renueva el
proveedor, sin gestionar llaves TLS dentro del backend.

---

## Licencia

Proyecto académico — Universidad Nacional de Colombia. Uso educativo (MIT).
